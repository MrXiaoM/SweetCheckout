package com.alipay.api.response;

import com.alipay.api.AlipayResponse;
import com.alipay.api.internal.mapping.ApiField;

/**
 * ALIPAY API: alipay.data.dataservice.bill.downloadurl.query response.
 *
 * @author auto create
 * @since 1.0, 2024-07-04 17:31:49
 */
public class AlipayDataDataserviceBillDownloadurlQueryResponse extends AlipayResponse {

    private static final long serialVersionUID = 2367819685665757396L;

    /**
     * 账单下载地址链接，获取连接后30秒后未下载，链接地址失效。
     */
    @ApiField("bill_download_url")
    private String billDownloadUrl;

    /**
     * 描述本次申请的账单文件状态。
     * EMPTY_DATA_WITH_BILL_FILE：当天无账单业务数据&amp;可以获取到空数据账单文件。
     */
    @ApiField("bill_file_code")
    private String billFileCode;

    public void setBillDownloadUrl(String billDownloadUrl) {
        this.billDownloadUrl = billDownloadUrl;
    }

    public String getBillDownloadUrl() {
        return this.billDownloadUrl;
    }

    public void setBillFileCode(String billFileCode) {
        this.billFileCode = billFileCode;
    }

    public String getBillFileCode() {
        return this.billFileCode;
    }

}
