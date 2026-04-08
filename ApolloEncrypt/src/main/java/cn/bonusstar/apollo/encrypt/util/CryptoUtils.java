package cn.bonusstar.apollo.encrypt.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 加/解密工具类：RSA、AES
 *
 * @author raynor
 * @since 2022/11/10
 */
public class CryptoUtils {

    private CryptoUtils() {
    }

    private static final String UTF_8 = "UTF-8";

    private static final String AES = "AES";
    private static final String AES_CBC_CIPHER = "AES/CBC/PKCS5Padding";

    private static final String RSA = "RSA";

    public static byte[] encryptAES(String data, String key) {
        return encryptAES(data, key, key);
    }

    public static byte[] encryptAES(String data, byte[] key) {
        return encryptAES(data, key, key);
    }

    public static byte[] encryptAES(byte[] data, String key) {
        return encryptAES(data, key, key);
    }

    public static byte[] encryptAES(byte[] data, byte[] key) {
        return encryptAES(data, key, key);
    }

    public static byte[] encryptAES(String data, String key, String iv) {
        return encryptAES(data.getBytes(StandardCharsets.UTF_8), base64Decode(key), base64Decode(iv));
    }

    public static byte[] encryptAES(byte[] data, String key, String iv) {
        return encryptAES(data, base64Decode(key), base64Decode(iv));
    }

    public static byte[] encryptAES(String data, byte[] key, byte[] iv) {
        return encryptAES(data.getBytes(StandardCharsets.UTF_8), key, iv);
    }

    public static byte[] encryptAES(byte[] data, byte[] key, byte[] iv) {
        return encryptAES(data, key, iv, AES_CBC_CIPHER);
    }

    /**
     * AES加密
     *
     * @param data
     * @param key
     * @param iv
     * @param mode
     * @return
     */
    public static byte[] encryptAES(byte[] data, byte[] key, byte[] iv, String mode) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, AES);
            Cipher cipher = Cipher.getInstance(mode);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static byte[] decryptAES(String data, String key) {
        return decryptAES(data, key, key);
    }

    public static byte[] decryptAES(String data, byte[] key) {
        return decryptAES(data, key, key);
    }

    public static byte[] decryptAES(byte[] data, String key) {
        return decryptAES(data, key, key);
    }

    public static byte[] decryptAES(byte[] data, byte[] key) {
        return decryptAES(data, key, key);
    }

    public static byte[] decryptAES(String data, String key, String iv) {
        return decryptAES(base64Decode(data), base64Decode(key), base64Decode(iv));
    }

    public static byte[] decryptAES(byte[] data, String key, String iv) {
        return decryptAES(data, base64Decode(key), base64Decode(iv));
    }

    public static byte[] decryptAES(String data, byte[] key, byte[] iv) {
        return decryptAES(base64Decode(data), key, iv);
    }

    public static byte[] decryptAES(byte[] data, byte[] key, byte[] iv) {
        return decryptAES(data, key, iv, AES_CBC_CIPHER);
    }

    /**
     * AES解密
     *
     * @param data
     * @param key
     * @param iv
     * @param mode
     * @return
     */
    public static byte[] decryptAES(byte[] data, byte[] key, byte[] iv, String mode) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, AES);
            Cipher cipher = Cipher.getInstance(mode);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * base64编码
     *
     * @param data
     * @return
     */
    public static String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * base64解码
     *
     * @param str
     * @return
     */
    public static byte[] base64Decode(String str) {
        return Base64.getDecoder().decode(str);
    }


    public static byte[] encryptRSA(String data, String publicKey) {
        return encryptRSA(data.getBytes(StandardCharsets.UTF_8), publicKey);
    }

    /**
     * RSA加密
     *
     * @param data
     * @param publicKey
     * @return
     */
    public static byte[] encryptRSA(byte[] data, String publicKey) {
        try {
            byte[] publicKeyBytes = base64Decode(publicKey);
            RSAPublicKey rsaPublicKey = (RSAPublicKey) KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            return encryptRSA(data, rsaPublicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * RSA加密
     *
     * @param data
     * @param publicKey
     * @return
     */
    public static byte[] encryptRSA(byte[] data, RSAPublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static byte[] decryptRSA(String data, String privateKey) {
        return decryptRSA(base64Decode(data), privateKey);
    }

    /**
     * RSA解密
     *
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] decryptRSA(byte[] data, String privateKey) {
        try {
            byte[] privateKeyBytes = base64Decode(privateKey);
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            return decryptRSA(data, rsaPrivateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * RSA解密
     *
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] decryptRSA(byte[] data, RSAPrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成RSA 公钥/私钥对，获取对应字符串
     *
     * @param keySize 长度，建议2048
     * @return
     */
    public static Map<String, String> generateKeyPair(int keySize) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
            keyPairGenerator.initialize(keySize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            Map<String, String> keys = new HashMap<>();
            keys.put("publicKey", base64Encode(keyPair.getPublic().getEncoded()));
            keys.put("privateKey", base64Encode(keyPair.getPrivate().getEncoded()));

            return keys;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
