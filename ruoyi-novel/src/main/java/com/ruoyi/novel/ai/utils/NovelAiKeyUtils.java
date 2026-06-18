package com.ruoyi.novel.ai.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import com.ruoyi.common.utils.StringUtils;

/**
 * AI API Key º”Ω‚√‹”ÎÕ—√Ù
 */
public final class NovelAiKeyUtils
{
    private static final String ALGORITHM = "AES";

    private static final String MASK_PLACEHOLDER = "******";

    private NovelAiKeyUtils()
    {
    }

    public static String encrypt(String plainText, String secret)
    {
        if (StringUtils.isEmpty(plainText))
        {
            return plainText;
        }
        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, buildKey(secret));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("API Key º”√‹ ß∞‹", ex);
        }
    }

    public static String decrypt(String cipherText, String secret)
    {
        if (StringUtils.isEmpty(cipherText))
        {
            return cipherText;
        }
        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, buildKey(secret));
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("API Key Ω‚√‹ ß∞‹", ex);
        }
    }

    public static String mask(String apiKey)
    {
        if (StringUtils.isEmpty(apiKey))
        {
            return "";
        }
        if (apiKey.length() <= 8)
        {
            return MASK_PLACEHOLDER;
        }
        return apiKey.substring(0, 3) + "****" + apiKey.substring(apiKey.length() - 4);
    }

    public static boolean isMaskedValue(String apiKey)
    {
        return StringUtils.isEmpty(apiKey) || MASK_PLACEHOLDER.equals(apiKey) || apiKey.contains("****");
    }

    private static SecretKeySpec buildKey(String secret)
    {
        byte[] keyBytes = new byte[16];
        byte[] secretBytes = StringUtils.defaultString(secret).getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < keyBytes.length; i++)
        {
            keyBytes[i] = i < secretBytes.length ? secretBytes[i] : 0;
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}
