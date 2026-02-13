package top.mrxiaom.sweet.checkout.func;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.checkout.PluginCommon;
import top.mrxiaom.sweet.checkout.func.entry.PaymentInfo;
import top.mrxiaom.sweet.checkout.packets.common.IPacket;
import top.mrxiaom.sweet.checkout.packets.common.IResponsePacket;
import top.mrxiaom.sweet.checkout.packets.plugin.PacketPluginCancelOrder;
import top.mrxiaom.sweet.checkout.packets.plugin.PacketPluginRequestOrder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static top.mrxiaom.sweet.checkout.func.PaymentsAndQRCodeManager.getMapColor;

@AutoRegister
public class MockPaymentManager extends AbstractModule implements IPaymentManager {
    private final Map<String, Double> orders = new HashMap<>();
    private final Map<UUID, String> processPlayers = new HashMap<>();
    private final byte mapDarkColor = getMapColor(8, 2);
    private final byte mapLightColor = getMapColor(29, 2);
    public MockPaymentManager(PluginCommon plugin) {
        super(plugin);
    }

    @Override
    public void putProcess(Player player, String tag) {
        processPlayers.put(player.getUniqueId(), tag);
    }

    @Override
    public boolean isProcess(Player player) {
        UUID uuid = player.getUniqueId();
        return processPlayers.containsKey(uuid);
    }

    @Override
    public PaymentInfo remove(Player player) {
        UUID uuid = player.getUniqueId();
        processPlayers.remove(uuid);
        return null;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <R extends IResponsePacket> void sendPacket(Player player, IPacket<R> packet, Consumer<R> resp) {
        // 解析即将发送的包，返回模拟结果
        if (packet instanceof PacketPluginRequestOrder) {
            PacketPluginRequestOrder request = (PacketPluginRequestOrder) packet;
            Double money = Util.parseDouble(request.getPrice()).orElse(null);
            if (money == null) return;
            String orderId = "MOCK_ORDER_" + UUID.randomUUID().toString().substring(0, 8);
            orders.put(orderId, money);
            resp.accept((R) new PacketPluginRequestOrder.Response("mock", orderId, String.format("%.2f", money), "MOCK_PAYMENT_URL"));
        }
        if (packet instanceof PacketPluginCancelOrder) {
            PacketPluginCancelOrder request = (PacketPluginCancelOrder) packet;
            orders.remove(request.getOrderId());
            resp.accept((R) new PacketPluginCancelOrder.Response());
        }
    }

    @Override
    public void requireScan(Player player, byte[] colors, String orderId, long outdateTime, Consumer<Double> done) {
        // 要求扫描时，直接调用回调方法
        Double money = orders.get(orderId);
        if (money != null) {
            remove(player);
            done.accept(money);
        }
    }

    @Override
    public byte getMapDarkColor() {
        return mapDarkColor;
    }

    @Override
    public byte getMapLightColor() {
        return mapLightColor;
    }

    @Override
    public byte[] getMapDarkPattern() {
        return null;
    }

    @Override
    public byte[] getMapLightPattern() {
        return null;
    }

    public static MockPaymentManager inst() {
        return instanceOf(MockPaymentManager.class);
    }
}
