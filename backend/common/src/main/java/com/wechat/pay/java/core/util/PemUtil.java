package com.wechat.pay.java.core.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static com.wechat.pay.java.core.cipher.Constant.HEX;

/**
 * PEM工具
 */
public class PemUtil {

    private PemUtil() {
    }

    /**
     * 从字符串中加载RSA私钥。
     *
     * @param keyString 私钥字符串
     * @return RSA私钥
     */
    public static PrivateKey loadPrivateKeyFromString(String keyString) {
        try {
            keyString =
                    keyString
                            .replace("-----BEGIN PRIVATE KEY-----", "")
                            .replace("-----END PRIVATE KEY-----", "")
                            .replaceAll("\\s+", "");
            return KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyString)));
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 从字符串中加载指定算法的私钥
     *
     * @param keyString 私钥字符串
     * @param algorithm 私钥算法
     * @param provider  the provider
     * @return 私钥
     */
    public static PrivateKey loadPrivateKeyFromString(
            String keyString, String algorithm, String provider) {
        try {
            keyString =
                    keyString
                            .replace("-----BEGIN PRIVATE KEY-----", "")
                            .replace("-----END PRIVATE KEY-----", "")
                            .replaceAll("\\s+", "");
            return KeyFactory.getInstance(algorithm, provider)
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyString)));
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvalidKeySpecException | NoSuchProviderException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 从字符串中加载RSA公钥。
     *
     * @param keyString 公钥字符串
     * @return RSA公钥
     */
    public static PublicKey loadPublicKeyFromString(String keyString) {
        try {
            keyString =
                    keyString
                            .replace("-----BEGIN PUBLIC KEY-----", "")
                            .replace("-----END PUBLIC KEY-----", "")
                            .replaceAll("\\s+", "");
            return KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(keyString)));
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 从文件路径加载RSA私钥
     *
     * @param keyPath 私钥路径
     * @return RSA私钥
     */
    public static PrivateKey loadPrivateKeyFromPath(String keyPath) {
        return loadPrivateKeyFromString(readKeyStringFromPath(keyPath));
    }

    /**
     * 从文件路径加载指定算法的私钥
     *
     * @param keyPath   私钥路径
     * @param algorithm 私钥算法
     * @param provider  the provider
     * @return 私钥
     */
    public static PrivateKey loadPrivateKeyFromPath(
            String keyPath, String algorithm, String provider) {
        return loadPrivateKeyFromString(readKeyStringFromPath(keyPath), algorithm, provider);
    }

    /**
     * 从文件路径加载RSA公钥
     *
     * @param keyPath 公钥路径
     * @return RSA公钥
     */
    public static PublicKey loadPublicKeyFromPath(String keyPath) {
        return loadPublicKeyFromString(readKeyStringFromPath(keyPath));
    }

    private static String readKeyStringFromPath(String keyPath) {
        try (FileInputStream inputStream = new FileInputStream(keyPath)) {
            return IOUtil.toString(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 从输入流加载X.509证书
     *
     * @param inputStream 私钥输入流
     * @return X.509证书
     */
    public static X509Certificate loadX509FromStream(InputStream inputStream) {
        try {
            return (X509Certificate)
                    CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
        } catch (CertificateException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 从输入流加载X.509证书
     *
     * @param inputStream 私钥输入流
     * @param provider    the provider
     * @return X.509证书
     */
    public static X509Certificate loadX509FromStream(InputStream inputStream, String provider) {
        try {
            return (X509Certificate)
                    CertificateFactory.getInstance("X.509", provider).generateCertificate(inputStream);
        } catch (CertificateException | NoSuchProviderException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 从文件路径加载X.509证书
     *
     * @param certificatePath 证书文件路径
     * @return X.509证书
     */
    public static X509Certificate loadX509FromPath(String certificatePath) {
        try (FileInputStream inputStream = new FileInputStream(certificatePath)) {
            return loadX509FromStream(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 从文件路径加载X.509证书
     *
     * @param certificatePath 证书文件路径
     * @param provider        the provider
     * @return X.509证书
     */
    public static X509Certificate loadX509FromPath(String certificatePath, String provider) {
        try (FileInputStream inputStream = new FileInputStream(certificatePath)) {
            return loadX509FromStream(inputStream, provider);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 从字符串加载X.509证书
     *
     * @param certificateString 证书字符串
     * @return X.509证书
     */
    public static X509Certificate loadX509FromString(String certificateString) {
        try (ByteArrayInputStream inputStream =
                     new ByteArrayInputStream(certificateString.getBytes(StandardCharsets.UTF_8))) {
            return loadX509FromStream(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 从字符串加载X.509证书
     *
     * @param certificateString 证书字符串
     * @param provider          the provider
     * @return X.509证书
     */
    public static X509Certificate loadX509FromString(String certificateString, String provider) {
        try (ByteArrayInputStream inputStream =
                     new ByteArrayInputStream(certificateString.getBytes(StandardCharsets.UTF_8))) {
            return loadX509FromStream(inputStream, provider);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String getSerialNumber(X509Certificate certificate) {
        return certificate.getSerialNumber().toString(HEX).toUpperCase();
    }
}
