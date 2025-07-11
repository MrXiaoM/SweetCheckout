package com.alipay.api.response;

import com.alipay.api.AlipayResponse;
import com.alipay.api.internal.mapping.ApiField;

/**
 * ALIPAY API: alipay.trade.precreate response.
 *
 * @author auto create
 * @since 1.0, 2024-08-15 11:52:06
 */
public class AlipayTradePrecreateResponse extends AlipayResponse {

    private static final long serialVersionUID = 3413295651213217771L;

    /**
     * 商户的订单号
     */
    @ApiField("out_trade_no")
    private String outTradeNo;

    /**
     * 当前预下单请求生成的二维码码串，有效时间2小时，可以用二维码生成工具根据该码串值生成对应的二维码
     */
    @ApiField("qr_code")
    private String qrCode;

    /**
     * 当前预下单请求生成的吱口令码串，有效时间2小时，可以在支付宝app端访问对应内容
     */
    @ApiField("share_code")
    private String shareCode;

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getOutTradeNo() {
        return this.outTradeNo;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getQrCode() {
        return this.qrCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    public String getShareCode() {
        return this.shareCode;
    }

}
