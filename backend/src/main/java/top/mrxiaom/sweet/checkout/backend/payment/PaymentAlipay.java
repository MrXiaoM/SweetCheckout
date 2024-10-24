package top.mrxiaom.sweet.checkout.backend.payment;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import top.mrxiaom.sweet.checkout.backend.Configuration;
import top.mrxiaom.sweet.checkout.backend.ConsoleMain;
import top.mrxiaom.sweet.checkout.backend.PaymentServer;
import top.mrxiaom.sweet.checkout.backend.data.ClientInfo;
import top.mrxiaom.sweet.checkout.packets.backend.PacketBackendPaymentCancel;
import top.mrxiaom.sweet.checkout.packets.backend.PacketBackendPaymentConfirm;
import top.mrxiaom.sweet.checkout.packets.plugin.PacketPluginRequestOrder;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class PaymentAlipay {
    PaymentServer server;
    public Map<String, ClientInfo.Order> moneyLocked = new HashMap<>();
    public PaymentAlipay(PaymentServer server) {
        this.server = server;
    }

    // TODO: 暂无Hook实现计划，先把接口摆在这
    public PacketPluginRequestOrder.Response handleHook(PacketPluginRequestOrder packet, ClientInfo client, Configuration config) {
        Configuration.AlipayHook hook = config.getHook().getAlipay();
        if (moneyLocked.containsKey(packet.getPrice())) {
            return new PacketPluginRequestOrder.Response("payment.hook-price-locked");
        }
        String orderId = client.nextOrderId();
        String paymentUrl = hook.getPaymentUrls().getOrDefault(packet.getPrice(), hook.getPaymentUrl());
        ClientInfo.Order order = client.createOrder(orderId, "alipay", packet.getPlayerName(), packet.getPrice());
        moneyLocked.put(packet.getPrice(), order);
        return new PacketPluginRequestOrder.Response("hook", orderId, paymentUrl);
    }

    public PacketPluginRequestOrder.Response handleFaceToFace(PacketPluginRequestOrder packet, ClientInfo client, Configuration config) {
        String orderId = client.nextOrderId();
        if (orderId == null) {
            return new PacketPluginRequestOrder.Response("payment.can-not-create-id");
        }
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(config.getAlipayFaceToFace().getConfig());

            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
            model.setOutTradeNo(orderId);
            model.setTotalAmount(packet.getPrice());
            model.setSubject(packet.getProductName());
            model.setProductCode("FACE_TO_FACE_PAYMENT");
            model.setBody(packet.getProductName());

            request.setBizModel(model);

            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            System.out.println(response.getBody());

            if (response.isSuccess()) {
                ClientInfo.Order order = client.createOrder(orderId, "alipay", packet.getPlayerName(), packet.getPrice());
                String outTradeNo = response.getOutTradeNo();
                order.setCancelAction(() -> cancelAlipayFaceToFace(outTradeNo));
                // 轮询检查是否交易成功
                order.setTask(new TimerTask() {
                    @Override
                    public void run() {
                        checkAlipayFaceToFace(client, this, orderId, outTradeNo);
                    }
                });
                // 每3秒检查一次是否支付成功
                server.getTimer().schedule(order.getTask(), 1000L, 3000L);
                return new PacketPluginRequestOrder.Response("face2face", orderId, response.getQrCode());
            } else {
                client.removeOrder(orderId);
                server.getLogger().warn("支付宝当面付调用失败");
                return new PacketPluginRequestOrder.Response("payment.internal-error");
            }
        } catch (AlipayApiException e) {
            client.removeOrder(orderId);
            server.getLogger().warn("支付宝当面付API执行错误", e);
            return new PacketPluginRequestOrder.Response("payment.internal-error");
        }
    }

    private void checkAlipayFaceToFace(ClientInfo client, TimerTask task, String orderId, String outTradeNo) {
        Configuration config = ConsoleMain.getConfig();
        ClientInfo.Order order = client.getOrder(orderId);
        if (order == null || !client.getWebSocket().isOpen()) { // 插件连接断开时、任务不存在时取消任务，并关闭交易
            task.cancel();
            if (order != null) {
                order.setTask(null);
                client.removeOrder(order);
            }
            cancelAlipayFaceToFace(outTradeNo);
            return;
        }
        try {
            // 统一收款订单交易查询
            AlipayClient alipayClient = new DefaultAlipayClient(config.getAlipayFaceToFace().getConfig());

            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(outTradeNo);
            request.setBizModel(model);

            AlipayTradeQueryResponse response = alipayClient.execute(request);
            System.out.println(response.getBody());
            if (response.isSuccess()) {
                String status = response.getTradeStatus();
                switch (status.toUpperCase()) {
                    case "WAIT_BUYER_PAY": // 等待买家付款
                        break;
                    case "TRADE_CLOSED": // 超时未付款，交易关闭
                        client.removeOrder(order);
                        server.send(client.getWebSocket(), new PacketBackendPaymentCancel(orderId, "payment.timeout"));
                        break;
                    case "TRADE_SUCCESS": // 交易支付成功
                    case "TRADE_FINISHED": {// 交易结束，不可退款
                        client.removeOrder(order);
                        // 买家支付宝账号，通常是打码的手机号
                        String buyerLogonId = response.getBuyerLogonId();
                        String money = response.getReceiptAmount();
                        server.getLogger().info("[收款] 从支付宝当面付收款，来自 {} 的 ￥{}", buyerLogonId, money);
                        server.send(client.getWebSocket(), new PacketBackendPaymentConfirm(orderId, money));
                        break;
                    }
                }
            } else {
                server.getLogger().warn("支付宝当面付检查订单失败");
            }
        } catch (AlipayApiException e) {
            server.getLogger().warn("支付宝当面付API检查订单时执行错误", e);
        }
    }

    private void cancelAlipayFaceToFace(String outTradeNo) {
        try {
            Configuration config = ConsoleMain.getConfig();
            AlipayClient alipayClient = new DefaultAlipayClient(config.getAlipayFaceToFace().getConfig());
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(outTradeNo);
            request.setBizModel(model);
            alipayClient.execute(request);
        } catch (AlipayApiException e) {
            server.getLogger().warn("支付宝当面付API关闭交易时执行错误", e);
        }
    }
}
