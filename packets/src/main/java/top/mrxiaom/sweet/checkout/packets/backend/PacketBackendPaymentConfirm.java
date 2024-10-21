package top.mrxiaom.sweet.checkout.packets.backend;

import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mrxiaom.sweet.checkout.packets.common.IPacket;
import top.mrxiaom.sweet.checkout.packets.common.NoResponse;

/**
 * 向插件端反馈订单已完成
 */
@Getter
@AllArgsConstructor
public class PacketBackendPaymentConfirm implements IPacket<NoResponse> {
    /**
     * 订单ID
     */
    private String orderId;
    /**
     * 付款人名字
     */
    private String name;
    /**
     * 支付的金额
     */
    private String money;

    @Override
    public Class<NoResponse> getResponsePacket() {
        return NoResponse.class;
    }
}