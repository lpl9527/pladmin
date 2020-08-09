package com.lpl.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;

/**
 * @author lpl
 * 加密工具类
 */
public class EncryptUtils {

    private static final String STR_PARAM = "Passw0rd";
    private static Cipher cipher;
    private static final IvParameterSpec IV = new IvParameterSpec(STR_PARAM.getBytes(StandardCharsets.UTF_8));

    public static void main(String[] args) throws Exception{

        System.out.println("-----------------DES对称加密、解密开始----------------------");
        String text  = "csvewakqhtoyijbd";    //明文
        System.out.println("明文为：" + text);

        //DES对称加密
        String encryptText = desEncrypt(text);
        System.out.println("DES对称加密后的结果为：" + encryptText);

        //DES对称解密
        String decryptText = desDecrypt(encryptText);
        System.out.println("DES对称解密后的结果为：" + decryptText);

        System.out.println("-----------------DES对称加密、解密结束----------------------");

    }

    /**
     * 根据要加密的文本生成DESKeySpec对象
     * @param source
     * @throws Exception
     */
    private static DESKeySpec getDesKeySpec(String source) throws Exception {
        if (null == source || source.length() == 0) {
            return null;
        }
        cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        String strKey = "Passw0rd";
        return new DESKeySpec(strKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * DES对称加密
     * @param source  待加密文本
     * @return  加密后的文本
     * @throws Exception
     */
    public static String desEncrypt(String source) throws Exception {
        DESKeySpec desKeySpec = getDesKeySpec(source);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IV);

        return byte2hex(
                cipher.doFinal(source.getBytes(StandardCharsets.UTF_8))).toUpperCase();
    }

    /**
     * DES对称解密
     * @param source 待解密文本
     * @return  解密后文本
     * @throws Exception
     */
    public static String desDecrypt(String source) throws Exception {
        byte[] src = hex2byte(source);
        DESKeySpec desKeySpec = getDesKeySpec(source);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IV);
        byte[] retByte = cipher.doFinal(src);
        return new String(retByte);
    }

    /**
     * 字节数组转化为哈希字符串
     * @param bytes
     */
    private static String byte2hex(byte[] bytes) {
        String stmp;
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            stmp = Integer.toHexString(b & 0xFF);
            if (stmp.length() == 1) {
                // 如果是0至F的单位字符串，则添加0
                out.append("0").append(stmp);
            }else {
                out.append(stmp);
            }
        }
        return out.toString();
    }

    /**
     * 将哈希字符串转换文字节数组
     * @param source  哈希字符串
     */
    private static byte[] hex2byte(String source) {
        byte[] b = source.getBytes();
        int size = 2;
        if ((b.length % size) != 0){
            throw new IllegalArgumentException("长度不是偶数");
        }
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += size) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }
}
