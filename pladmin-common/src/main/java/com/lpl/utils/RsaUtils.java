package com.lpl.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author lpl
 * Rsa加密、解密工具类（含主方法测试）
 */
public class RsaUtils {

    private static final String SRC = "123456";       //待测试的原字符串

    /**
     * 公钥私钥测试主方法
     */
    public static void main(String[] args) throws Exception{
        System.out.println("\n");
        //生成公钥、私钥对
        RsaKeyPair keyPair = generateKeyPair();
        System.out.println("生成的公钥：" + keyPair.getPublicKey());
        System.out.println("生成的私钥：" + keyPair.getPrivateKey());

        System.out.println("\n");
        //测试公钥加密、私钥解密
        test1(keyPair);

        System.out.println("\n");
        //测试私钥解密、公钥解密
        test2(keyPair);

        System.out.println("\n");
    }

    /**
     * Rsa密钥对象
     */
    public static class RsaKeyPair {

        private final String publicKey;     //公钥
        private final String privateKey;    //私钥

        public RsaKeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public String getPublicKey() {
            return this.publicKey;
        }
        public String getPrivateKey() {
            return this.privateKey;
        }
    }

    /**
     * 生成RSA密钥对
     * @throws NoSuchAlgorithmException 没有此算法异常
     */
    public static RsaKeyPair generateKeyPair() throws NoSuchAlgorithmException {
        //指定算法
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        //初始化key大小
        keyPairGenerator.initialize(1024);
        //生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //获取公钥
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        String publicKey = Base64.encodeBase64String(rsaPublicKey.getEncoded());
        //获取私钥
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        String privateKey = Base64.encodeBase64String(rsaPrivateKey.getEncoded());

        return new RsaKeyPair(publicKey, privateKey);
    }

    /**
     * 测试公钥加密私钥解密
     * @param keyPair   密钥对对象
     * @throws Exception
     */
    public static void test1(RsaKeyPair keyPair) throws Exception {
        System.out.println("***************** 公钥加密私钥解密开始 *****************");

        System.out.println("加密前：" + RsaUtils.SRC);
        //公钥加密文本
        String text1 = encryptByPublicKey(keyPair.getPublicKey(), RsaUtils.SRC);
        System.out.println("公钥加密后：" + text1);
        //私钥解密文本
        String text2 = decryptByPrivateKey(keyPair.getPrivateKey(), text1);
        System.out.println("私钥解密后：" + text2);

        if (RsaUtils.SRC.equals(text2)) {
            System.out.println("解密字符串和原始字符串一致，解密成功！");
        }else {
            System.out.println("解密字符串和原始字符串不一致，解密失败！");
        }
        System.out.println("***************** 公钥加密私钥解密结束 *****************");
    }

    public static void test2(RsaKeyPair keyPair) throws Exception {
        System.out.println("***************** 私钥加密公钥解密开始 *****************");

        System.out.println("加密前：" + RsaUtils.SRC);
        //私钥加密文本
        String text1 = encryptByPrivateKey(keyPair.getPrivateKey(), RsaUtils.SRC);
        System.out.println("私钥加密后：" + text1);
        String text2 = decryptByPublicKey(keyPair.getPublicKey(), text1);
        System.out.println("公钥解密后：" + text2);

        if (RsaUtils.SRC.equals(text2)) {
            System.out.println("解密字符串和原始字符串一致，解密成功！");
        } else {
            System.out.println("解密字符串和原始字符串不一致，解密失败！");
        }
        System.out.println("***************** 私钥加密公钥解密结束 *****************");
    }

    /**
     * 公钥加密
     * @param publicKeyText  公钥
     * @param text  待加密文本
     * @return  加密后的结果字符串
     * @throws Exception
     */
    public static String encryptByPublicKey(String publicKeyText, String text) throws Exception {

        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyText));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] result = cipher.doFinal(text.getBytes());

        return Base64.encodeBase64String(result);
    }

    /**
     * 私钥解密
     * @param privateKeyText    私钥
     * @param text  待解密文本
     * @return  解密的文本
     * @throws Exception
     */
    public static String decryptByPrivateKey(String privateKeyText, String text) throws Exception {

        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyText));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] result = cipher.doFinal(Base64.decodeBase64(text));

        return new String(result);
    }

    //----------------------------------------------------------------------------------------

    /**
     * 私钥加密
     * @param privateKeyText    私钥
     * @param text  待加密文本
     * @return  加密后字符串
     * @throws Exception
     */
    public static String encryptByPrivateKey(String privateKeyText, String text) throws Exception {

        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyText));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] result = cipher.doFinal(text.getBytes());

        return Base64.encodeBase64String(result);
    }

    /**
     * 公钥解密
     * @param publicKeyText 公钥
     * @param text  待解密文本
     * @return  解密后文本
     * @throws Exception
     */
    public static String decryptByPublicKey(String publicKeyText, String text) throws Exception {

        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyText));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] result = cipher.doFinal(Base64.decodeBase64(text));

        return new String(result);
    }
}
