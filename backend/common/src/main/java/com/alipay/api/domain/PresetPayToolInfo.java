package com.alipay.api.domain;

import com.alipay.api.AlipayObject;
import com.alipay.api.internal.mapping.ApiField;
import com.alipay.api.internal.mapping.ApiListField;

import java.util.List;

/**
 * 前置支付资产信息
 *
 * @author auto create
 * @since 1.0, 2023-07-11 20:10:55
 */
public class PresetPayToolInfo extends AlipayObject {

    private static final long serialVersionUID = 2281323595553396341L;

    /**
     * 前置资产金额
     */
    @ApiListField("amount")
    @ApiField("price")
    private List<String> amount;

    /**
     * 前置资产类型编码，和收单支付传入的preset_pay_tool里面的类型编码保持一致。
     */
    @ApiField("assert_type_code")
    private String assertTypeCode;

    public List<String> getAmount() {
        return this.amount;
    }

    public void setAmount(List<String> amount) {
        this.amount = amount;
    }

    public String getAssertTypeCode() {
        return this.assertTypeCode;
    }

    public void setAssertTypeCode(String assertTypeCode) {
        this.assertTypeCode = assertTypeCode;
    }

}
