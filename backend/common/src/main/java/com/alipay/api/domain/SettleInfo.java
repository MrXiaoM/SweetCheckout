package com.alipay.api.domain;

import com.alipay.api.AlipayObject;
import com.alipay.api.internal.mapping.ApiField;
import com.alipay.api.internal.mapping.ApiListField;

import java.util.List;

/**
 * 描述结算信息，json格式。
 *
 * @author auto create
 * @since 1.0, 2020-03-10 15:50:30
 */
public class SettleInfo extends AlipayObject {

    private static final long serialVersionUID = 4193174945885552692L;

    /**
     * 结算详细信息，json数组，目前只支持一条。
     */
    @ApiListField("settle_detail_infos")
    @ApiField("settle_detail_info")
    private List<SettleDetailInfo> settleDetailInfos;

    /**
     * 该笔订单的超期自动确认结算时间，到达期限后，将自动确认结算。此字段只在签约账期结算模式时有效。取值范围：1d～365d。d-天。 该参数数值不接受小数点。
     */
    @ApiField("settle_period_time")
    private String settlePeriodTime;

    public List<SettleDetailInfo> getSettleDetailInfos() {
        return this.settleDetailInfos;
    }

    public void setSettleDetailInfos(List<SettleDetailInfo> settleDetailInfos) {
        this.settleDetailInfos = settleDetailInfos;
    }

    public String getSettlePeriodTime() {
        return this.settlePeriodTime;
    }

    public void setSettlePeriodTime(String settlePeriodTime) {
        this.settlePeriodTime = settlePeriodTime;
    }

}
