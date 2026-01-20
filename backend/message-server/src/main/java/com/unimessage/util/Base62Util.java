package com.unimessage.util;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Base62 编码工具类
 * 用于生成短链码
 *
 * @author 海明
 * @since 2026-01-14
 */
public class Base62Util {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;

    private Base62Util() {
    }

    /**
     * 将数字编码为 Base62 字符串
     *
     * @param num 数字
     * @return Base62 字符串
     */
    public static String encode(long num) {
        if (num == 0) {
            return String.valueOf(BASE62_CHARS.charAt(0));
        }
        if (num < 0) {
            num = Math.abs(num);
        }

        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.insert(0, BASE62_CHARS.charAt((int) (num % BASE)));
            num /= BASE;
        }
        return sb.toString();
    }

    /**
     * 将 Base62 字符串解码为数字
     *
     * @param str Base62 字符串
     * @return 数字
     */
    public static long decode(String str) {
        long num = 0;
        for (int i = 0; i < str.length(); i++) {
            num = num * BASE + BASE62_CHARS.indexOf(str.charAt(i));
        }
        return num;
    }

    /**
     * 生成指定长度的随机 Base62 字符串
     * 使用 ThreadLocalRandom 保证线程安全和高性能
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(BASE62_CHARS.charAt(random.nextInt(BASE)));
        }
        return sb.toString();
    }

    /**
     * 生成指定长度的安全随机 Base62 字符串
     * 使用 SecureRandom，适用于安全性要求高的场景
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String secureRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            sb.append(BASE62_CHARS.charAt(random.nextInt(BASE)));
        }
        return sb.toString();
    }
}
