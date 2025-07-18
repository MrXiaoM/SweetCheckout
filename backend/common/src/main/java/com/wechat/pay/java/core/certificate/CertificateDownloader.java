package com.wechat.pay.java.core.certificate;

import com.wechat.pay.java.core.certificate.model.Data;
import com.wechat.pay.java.core.certificate.model.DownloadCertificateResponse;
import com.wechat.pay.java.core.certificate.model.EncryptCertificate;
import com.wechat.pay.java.core.cipher.AeadCipher;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.http.*;

import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class CertificateDownloader {
    private final CertificateHandler certificateHandler;
    private final AeadCipher aeadCipher;
    private final HttpClient httpClient;
    private final String downloadUrl;

    public CertificateDownloader(Builder builder) {
        this.downloadUrl = builder.downloadUrl;
        this.httpClient = builder.httpClient;
        this.aeadCipher = builder.aeadCipher;
        this.certificateHandler = builder.certificateHandler;
    }

    public static class Builder {
        private String downloadUrl;
        private HttpClient httpClient;
        private AeadCipher aeadCipher;
        private CertificateHandler certificateHandler;

        public Builder downloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder aeadCipher(AeadCipher cipher) {
            this.aeadCipher = cipher;
            return this;
        }

        public Builder certificateHandler(CertificateHandler handler) {
            this.certificateHandler = handler;
            return this;
        }

        public CertificateDownloader build() {
            requireNonNull(downloadUrl);
            requireNonNull(httpClient);
            requireNonNull(aeadCipher);
            requireNonNull(certificateHandler);
            return new CertificateDownloader(this);
        }
    }

    /**
     * 下载证书
     */
    public Map<String, X509Certificate> download() {
        HttpRequest httpRequest =
                new HttpRequest.Builder()
                        .httpMethod(HttpMethod.GET)
                        .url(downloadUrl)
                        .addHeader(Constant.ACCEPT, " */*")
                        .addHeader(Constant.CONTENT_TYPE, MediaType.APPLICATION_JSON.getValue())
                        .build();
        try {
            HttpResponse<DownloadCertificateResponse> httpResponse =
                    httpClient.execute(httpRequest, DownloadCertificateResponse.class);
            return decryptCertificate(httpResponse);
        } catch (ServiceException e) {
            // 如果证书不存在，可能是切换为平台公钥，该处不报错
            if (e.getErrorCode().equals("NOT_FOUND")) {
                return new HashMap<>();
            }
            throw e;
        }
    }

    /**
     * 从应答报文中解密证书
     *
     * @param httpResponse httpResponse
     * @return 应答报文解密后，生成X.509证书对象的Map
     */
    private Map<String, X509Certificate> decryptCertificate(
            HttpResponse<DownloadCertificateResponse> httpResponse) {
        List<Data> dataList = httpResponse.getServiceResponse().getData();
        Map<String, X509Certificate> downloadCertMap = new HashMap<>();
        for (Data data : dataList) {
            EncryptCertificate encryptCertificate = data.getEncryptCertificate();
            X509Certificate certificate;
            String decryptCertificate =
                    aeadCipher.decrypt(
                            encryptCertificate.getAssociatedData().getBytes(StandardCharsets.UTF_8),
                            encryptCertificate.getNonce().getBytes(StandardCharsets.UTF_8),
                            Base64.getDecoder().decode(encryptCertificate.getCiphertext()));

            certificate = certificateHandler.generateCertificate(decryptCertificate);
            downloadCertMap.put(data.getSerialNo(), certificate);
        }
        return downloadCertMap;
    }
}
