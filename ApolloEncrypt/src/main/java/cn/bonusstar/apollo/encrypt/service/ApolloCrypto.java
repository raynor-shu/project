package cn.bonusstar.apollo.encrypt.service;

import cn.bonusstar.apollo.encrypt.util.CryptoUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author raynor
 * @since 2025/7/17
 */
@Slf4j
public class ApolloCrypto {

    private static final byte[] salt = new byte[]{
            107, -22, -39, -84, -63, -86, -82, 85,
            -91, -115, 90, -124, -106, 100, -39, -98,
            17, 34, -68, -62, 78, 27, 92, -26,
            -44, -101, -127, -75, 35, 26, 74, -59};
    private static final Map<String, byte[]> aesKeyMap = new HashMap<>();
    private static final Map<String, byte[]> aesIvMap = new HashMap<>();
    private static final Map<String, String> lastKeyAndIvMap = new HashMap<>();
    @Getter
    private static String activeVersion;
    @Getter
    private static JSONObject keyAndIvJson;

    private static final String ENCRYPT_PREFIX = "$ENCRYPT$.";
    private static final String APOLLO_ENCRYPT_AESKEYANDIV_KEY = "APOLLO_ENCRYPT_AESKEYANDIV";
    private static final String ACTIVE_VERSION = "activeVersion";

    public static void initByEnv() {
        // 从环境变量初始化仅执行一次
        if (lastKeyAndIvMap.isEmpty()) {
            String keyAndIvJsonStr = System.getenv(APOLLO_ENCRYPT_AESKEYANDIV_KEY);
            if (StringUtils.isBlank(keyAndIvJsonStr)) {
                log.error("apollo加密密钥为空，请在系统变量内配置");
                return;
            }
            initByKeyAndIv(keyAndIvJsonStr);
        }
    }

    public static synchronized void initByKeyAndIv(String keyAndIvJsonStr) {
        try {
            JSONObject json = JSON.parseObject(new String(CryptoUtils.base64Decode(keyAndIvJsonStr), StandardCharsets.UTF_8));
            if (!json.containsKey(ACTIVE_VERSION)) {
                log.error("缺少" + ACTIVE_VERSION + "配置：" + keyAndIvJsonStr);
                return;
            }
            String activeVersion = json.getString(ACTIVE_VERSION);
            for (String key : json.keySet()) {
                if (ACTIVE_VERSION.equals(key)) {
                    continue;
                }
                String keyAndIv = json.getString(key);
                if (keyAndIv.equals(lastKeyAndIvMap.get(key))) {
                    return;
                }
                byte[] aesKeyAndIv = CryptoUtils.base64Decode(keyAndIv);
                if (aesKeyAndIv.length != 32) {
                    log.error("apollo加密aesKeyAndIv错误：" + key + "," + keyAndIv);
                    continue;
                }

                for (int i = 0; i < aesKeyAndIv.length; i++) {
                    aesKeyAndIv[i] ^= salt[i];
                }
                byte[] aesKey = new byte[16];
                byte[] aesIv = new byte[16];

                System.arraycopy(aesKeyAndIv, 0, aesKey, 0, 16);
                System.arraycopy(aesKeyAndIv, 16, aesIv, 0, 16);

                aesKeyMap.put(key, aesKey);
                aesIvMap.put(key, aesIv);
                lastKeyAndIvMap.put(key, keyAndIv);
            }

            if (!aesKeyMap.containsKey(activeVersion)) {
                log.error(ACTIVE_VERSION + "配置不能为空：" + activeVersion);
            }

            ApolloCrypto.activeVersion = activeVersion;
            ApolloCrypto.keyAndIvJson = json;
            log.info("ApolloCrypto init，activeVersion：" + activeVersion + ", " + json.getString(activeVersion));
        } catch (Exception ex) {
            log.error("apollo加密aesKeyAndIv解析失败：" + keyAndIvJsonStr + "，参照格式{'activeVersion':'v01','v01':'xxxxx','v02':'xxxx'}");
            return;
        }
    }

    public static String encrypt(String data) {
        return encrypt(data, activeVersion);
    }

    public static String encrypt(String data, String version) {
        byte[] activeAesKey = aesKeyMap.get(version);
        byte[] activeAesIv = aesIvMap.get(version);
        return ENCRYPT_PREFIX + version + "." + CryptoUtils.base64Encode(CryptoUtils.encryptAES(data, activeAesKey, activeAesIv));
    }

    public static String decrypt(String encryptData) {
        // 非加密数据直接返回
        if (!encryptData.startsWith(ENCRYPT_PREFIX)) {
            return encryptData;
        }

        // 解析版本
        int nextPointIndex = encryptData.indexOf(".", ENCRYPT_PREFIX.length());
        if (nextPointIndex == -1) {
            return encryptData;
        }
        String version = encryptData.substring(ENCRYPT_PREFIX.length(), nextPointIndex);
        // 根据版本查找密钥
        if (!aesKeyMap.containsKey(version)) {
            return encryptData;
        }
        byte[] aesKey = aesKeyMap.get(version);
        byte[] aesIv = aesIvMap.get(version);

        try {
            return new String(CryptoUtils.decryptAES(encryptData.substring(nextPointIndex + 1), aesKey, aesIv), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            // 解密失败，返回原文
            log.warn("apollo解密失败：" + encryptData);
            return encryptData;
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(randomKeyAndIv());
            return;
        }
        initByKeyAndIv(args[0]);
        for (int i = 2; i < args.length; i++) {
            if ("encrypt".equals(args[1])) {

                System.out.println(args[i] + "   -->   " + encrypt(StringEscapeUtils.unescapeJava(args[i])));
            } else {
                System.out.println(args[i] + "   -->   " + StringEscapeUtils.escapeJava(decrypt(args[i])));
            }
        }
    }

    public static String randomKeyAndIv() {
        byte[] keyAndIv = new byte[32];
        Random rand = new Random(System.currentTimeMillis());
        for (int i = 0; i < keyAndIv.length; i++) {
            keyAndIv[i] = (byte) (rand.nextInt(256) - 128);
        }

        return CryptoUtils.base64Encode(keyAndIv);
    }
}
