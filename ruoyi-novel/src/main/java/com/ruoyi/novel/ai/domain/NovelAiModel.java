package com.ruoyi.novel.ai.domain;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI 模型配置 novel_ai_model
 */
public class NovelAiModel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    public static final String PROVIDER_OPENAI = "openai";

    public static final String PROVIDER_ANTHROPIC = "anthropic";

    /** 模型配置ID */
    private Long modelId;

    /** 所属用户ID */
    private Long userId;

    /** 显示名称 */
    @Excel(name = "模型名称")
    private String modelName;

    /** 协议类型 */
    @Excel(name = "协议类型")
    private String providerType;

    /** API Base URL */
    private String baseUrl;

    /** API Key */
    private String apiKey;

    /** 模型标识 */
    @Excel(name = "模型标识")
    private String modelCode;

    /** 默认温度 */
    private BigDecimal temperature;

    /** 默认最大 Token */
    private Integer maxTokens;

    /** 请求超时毫秒 */
    private Integer timeoutMs;

    /** 是否激活 */
    @Excel(name = "是否激活", readConverterExp = "0=否,1=是")
    private String isActive;

    /** 是否默认 */
    private String isDefault;

    /** 排序 */
    private Integer sortOrder;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getModelId()
    {
        return modelId;
    }

    public void setModelId(Long modelId)
    {
        this.modelId = modelId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称不能超过100个字符")
    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    @NotBlank(message = "协议类型不能为空")
    public String getProviderType()
    {
        return providerType;
    }

    public void setProviderType(String providerType)
    {
        this.providerType = providerType;
    }

    @NotBlank(message = "Base URL 不能为空")
    @Size(max = 500, message = "Base URL 不能超过500个字符")
    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    @NotBlank(message = "模型标识不能为空")
    @Size(max = 100, message = "模型标识不能超过100个字符")
    public String getModelCode()
    {
        return modelCode;
    }

    public void setModelCode(String modelCode)
    {
        this.modelCode = modelCode;
    }

    public BigDecimal getTemperature()
    {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature)
    {
        this.temperature = temperature;
    }

    public Integer getMaxTokens()
    {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens)
    {
        this.maxTokens = maxTokens;
    }

    public Integer getTimeoutMs()
    {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs)
    {
        this.timeoutMs = timeoutMs;
    }

    public String getIsActive()
    {
        return isActive;
    }

    public void setIsActive(String isActive)
    {
        this.isActive = isActive;
    }

    public String getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(String isDefault)
    {
        this.isDefault = isDefault;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("modelId", getModelId())
            .append("userId", getUserId())
            .append("modelName", getModelName())
            .append("providerType", getProviderType())
            .append("baseUrl", getBaseUrl())
            .append("modelCode", getModelCode())
            .append("temperature", getTemperature())
            .append("maxTokens", getMaxTokens())
            .append("timeoutMs", getTimeoutMs())
            .append("isActive", getIsActive())
            .append("isDefault", getIsDefault())
            .append("sortOrder", getSortOrder())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
