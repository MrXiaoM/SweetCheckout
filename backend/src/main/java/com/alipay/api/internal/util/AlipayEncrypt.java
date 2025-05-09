/**
 * Alipay.com Inc. Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.alipay.api.internal.util;

import com.alipay.api.AlipayApiErrorEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.encrypt.Encrypt;
import com.alipay.api.internal.util.encrypt.impl.AesEncrypt;
import com.alipay.api.internal.util.encrypt.impl.AesEncryptV2;
import com.alipay.api.internal.util.encrypt.impl.SM4Encrypt;

import java.util.HashMap;
import java.util.Map;

/**
 * 加密工具
 *
 * @author jiehua
 * @version $Id: AlipayEncrypt.java, v 0.1 2016-3-28 下午5:14:12 jiehua Exp $
 */
public class AlipayEncrypt {

    private static Map<String, Encrypt> encryptManager = new HashMap<>();

    static {
        encryptManager.put("AES", new AesEncrypt());
        encryptManager.put("AES_V2", new AesEncryptV2());
//        encryptManager.put("SM4", new SM4Encrypt());
    }


    /**
     * 加密
     */
    public static String encryptContent(String content, String encryptType, String encryptKey,
                                        String charset) throws AlipayApiException {

        Encrypt encrypt = encryptManager.get(encryptType);
        if (encrypt == null) {
            if ("SM4".equals(encryptType)) {
                // SM4Encrypt类需要使用时再加载，避免未引入BouncyCastleProvider导致AlipayEncrypt类加载失败
                encrypt = new SM4Encrypt();
            } else {
                throw new AlipayApiException(AlipayApiErrorEnum.ENCRYPT_TYPE_ERROR.getErrMsg() + encryptType);
            }
        }

        return encrypt.encrypt(content, encryptKey, charset);
    }

    /**
     * 解密
     */
    public static String decryptContent(String content, String encryptType, String encryptKey,
                                        String charset) throws AlipayApiException {
        Encrypt encrypt = encryptManager.get(encryptType);
        if (encrypt == null) {
            if ("SM4".equals(encryptType)) {
                // SM4Encrypt类需要使用时再加载，避免未引入BouncyCastleProvider导致AlipayEncrypt类加载失败
                encrypt = new SM4Encrypt();
            } else {
                throw new AlipayApiException(AlipayApiErrorEnum.ENCRYPT_TYPE_ERROR.getErrMsg() + encryptType);
            }
        }

        return encrypt.decrypt(content, encryptKey, charset);
    }


    /**
     * Getter method for property <tt>encryptManager</tt>.
     *
     * @return property value of encryptManager
     */
    public static Map<String, Encrypt> getEncryptManager() {
        return encryptManager;
    }

    public static void putEncryptManager(String encryptType, Encrypt encrypt) {
        encryptManager.put(encryptType, encrypt);
    }
}
