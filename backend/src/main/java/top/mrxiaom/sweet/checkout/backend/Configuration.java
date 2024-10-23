package top.mrxiaom.sweet.checkout.backend;

import com.alipay.api.AlipayConfig;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Configuration {
    private static Logger logger = LoggerFactory.getLogger(Configuration.class);
    private int port = 62233;
    @SerializedName("wechat_native")
    private WeChatNative weChatNative = new WeChatNative();
    @SerializedName("alipay_face2face")
    private AlipayFaceToFace alipayFaceToFace = new AlipayFaceToFace();
    private Hook hook = new Hook();

    private static String parseString(Logger logger, String name, String str) {
        if (!str.startsWith("file:")) return str;
        String path = str.substring(5);
        File file = new File(path);
        if (!file.exists()) {
            logger.warn("无法从配置 {} 找到对应文件 {}", name, path);
            return null;
        }
        try {
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            String log = "读取文件 " + path + " 时出现一个异常";
            logger.warn(log, e);
            return null;
        }
    }

    protected void postLoad() {
        getWeChatNative().postLoad();
        getAlipayFaceToFace().postLoad();
    }

    public int getPort() {
        return port;
    }

    public WeChatNative getWeChatNative() {
        return weChatNative;
    }

    public AlipayFaceToFace getAlipayFaceToFace() {
        return alipayFaceToFace;
    }

    public Hook getHook() {
        return hook;
    }

    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public static class WeChatNative {
        private boolean enable = false;
        @SerializedName("api_key")
        private String apiKey = "商户API V3密钥";
        @SerializedName("merchant_id")
        private String merchantId = "商户号";
        @SerializedName("merchant_serial_number")
        private String merchantSerialNumber = "商户证书序列号";
        @SerializedName("private_key")
        private String privateKey = "file:secrets/wechat/private.txt";
        @SerializedName("notify_url")
        private String notifyUrl = "https://mcio.dev/consumer/notify";
        @SerializedName("sp_app_id")
        private String spAppId = "服务商应用ID";
        @SerializedName("sp_merchant_id")
        private String spMerchantId = "服务商户号";
        @SerializedName("sub_merchant_id")
        private String subMerchantId = "子商户号";

        @Expose(serialize = false, deserialize = false)
        private Config config;
        private void postLoad() {
            if (isEnable()) {
                String privateKeyStr = getPrivateKey();
                String privateKey = parseString(logger, "alipayFaceToFace.privateKey", privateKeyStr);
                if (privateKey == null) return;
                config = new RSAAutoCertificateConfig.Builder()
                        .merchantId(getMerchantId())
                        .privateKey(privateKey)
                        .merchantSerialNumber(getMerchantSerialNumber())
                        .apiV3Key(getApiKey())
                        .build();
            }
        }
        public boolean isEnable() {
            return enable;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getMerchantId() {
            return merchantId;
        }

        public String getMerchantSerialNumber() {
            return merchantSerialNumber;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public String getNotifyUrl() {
            return notifyUrl;
        }

        public String getSpAppId() {
            return spAppId;
        }

        public String getSpMerchantId() {
            return spMerchantId;
        }

        public String getSubMerchantId() {
            return subMerchantId;
        }

        public Config getConfig() {
            return config;
        }
    }
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public static class AlipayFaceToFace {
        private boolean enable = false;
        @SerializedName("app_id")
        private String appId = "";
        @SerializedName("private_key")
        private String privateKey = "file:secrets/alipay/private.txt";
        @SerializedName("alipay_public_key")
        private String alipayPublicKey = "file:secrets/alipay/public.txt";

        @Expose(serialize = false, deserialize = false)
        private AlipayConfig config;
        private void postLoad() {
            if (isEnable()) {
                String privateKeyStr = getPrivateKey();
                String publicKeyStr = getAlipayPublicKey();
                String privateKey = parseString(logger, "alipayFaceToFace.privateKey", privateKeyStr);
                if (privateKey == null) return;
                String publicKey = parseString(logger, "alipayFaceToFace.alipayPublicKey", publicKeyStr);
                if (publicKey == null) return;
                AlipayConfig alipayConfig = new AlipayConfig();
                alipayConfig.setServerUrl("https://openapi.alipay.com/gateway.do");
                alipayConfig.setAppId(getAppId());
                alipayConfig.setPrivateKey(privateKey);
                alipayConfig.setFormat("json");
                alipayConfig.setAlipayPublicKey(publicKey);
                alipayConfig.setCharset("UTF-8");
                alipayConfig.setSignType("RSA2");
                this.config = alipayConfig;
            }
        }
        public boolean isEnable() {
            return enable;
        }

        public String getAppId() {
            return appId;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public String getAlipayPublicKey() {
            return alipayPublicKey;
        }

        public AlipayConfig getConfig() {
            return config;
        }
    }
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public static class Hook {
        private boolean enable = true;
        @SerializedName("end_point")
        private String endPoint = "/api/hook/receive";
        @SerializedName("wechat")
        private WeChatHook weChat = new WeChatHook();
        public boolean isEnable() {
            return enable;
        }

        public String getEndPoint() {
            return endPoint;
        }

        public WeChatHook getWeChat() {
            return weChat;
        }
    }
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public static class WeChatHook {
        private boolean enable = false;
        @SerializedName("require_process")
        private String requireProcess = "SweetCheckout.Hook.WeChat.exe";
        @SerializedName("payment_url")
        private String paymentUrl = "";
        @SerializedName("payment_urls")
        private Map<String, String> paymentUrls = new HashMap<>() {{
            put("1.00", "");
        }};

        public boolean isEnable() {
            return enable;
        }

        public String getRequireProcess() {
            return requireProcess;
        }

        public String getPaymentUrl() {
            return paymentUrl;
        }

        public Map<String, String> getPaymentUrls() {
            return paymentUrls;
        }
    }
}
