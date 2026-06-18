package com.ruoyi.novel.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ruoyi.novel.ai.utils.NovelAiKeyUtils;

/**
 * API Key 加解密（密钥取自 token.secret）
 */
@Component
public class NovelAiKeyCrypto
{
    @Value("${token.secret}")
    private String secret;

    public String encrypt(String plainText)
    {
        return NovelAiKeyUtils.encrypt(plainText, secret);
    }

    public String decrypt(String cipherText)
    {
        return NovelAiKeyUtils.decrypt(cipherText, secret);
    }

    public String mask(String plainText)
    {
        return NovelAiKeyUtils.mask(plainText);
    }
}
