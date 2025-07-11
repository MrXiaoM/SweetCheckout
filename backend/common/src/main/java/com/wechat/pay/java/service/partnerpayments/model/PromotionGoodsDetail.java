// Copyright 2021 Tencent Inc. All rights reserved.
//
// APP支付
//
// APP支付API
//
// API version: 1.2.3

// Code generated by WechatPay APIv3 Generator based on [OpenAPI
// Generator](https://openapi-generator.tech); DO NOT EDIT.

package com.wechat.pay.java.service.partnerpayments.model;

import com.google.gson.annotations.SerializedName;

import static com.wechat.pay.java.core.util.StringUtil.toIndentedString;

/**
 * PromotionGoodsDetail
 */
public class PromotionGoodsDetail {
    /**
     * 商品编码 说明：商品编码
     */
    @SerializedName("goods_id")
    private String goodsId;

    /**
     * 商品数量 说明：商品数量
     */
    @SerializedName("quantity")
    private Integer quantity;

    /**
     * 商品价格 说明：商品价格
     */
    @SerializedName("unit_price")
    private Integer unitPrice;

    /**
     * 商品优惠金额 说明：商品优惠金额
     */
    @SerializedName("discount_amount")
    private Integer discountAmount;

    /**
     * 商品备注 说明：商品备注
     */
    @SerializedName("goods_remark")
    private String goodsRemark;

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Integer unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getGoodsRemark() {
        return goodsRemark;
    }

    public void setGoodsRemark(String goodsRemark) {
        this.goodsRemark = goodsRemark;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PromotionGoodsDetail {\n");
        sb.append("    goodsId: ").append(toIndentedString(goodsId)).append("\n");
        sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
        sb.append("    unitPrice: ").append(toIndentedString(unitPrice)).append("\n");
        sb.append("    discountAmount: ").append(toIndentedString(discountAmount)).append("\n");
        sb.append("    goodsRemark: ").append(toIndentedString(goodsRemark)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
