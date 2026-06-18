package com.ruoyi.novel.agent;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring-ai-agent-utils 工具注册（TodoWrite 等）
 */
@Configuration
public class NovelAgentUtilsConfig
{
    @Bean
    public ToolCallback[] novelAgentUtilToolCallbacks()
    {
        try
        {
            Class<?> todoClass = Class.forName("org.springaicommunity.ai.agent.tools.TodoWriteTool");
            Object builder = todoClass.getMethod("builder").invoke(null);
            Object tool = builder.getClass().getMethod("build").invoke(builder);
            if (tool instanceof ToolCallback)
            {
                return new ToolCallback[] { (ToolCallback) tool };
            }
            if (tool != null)
            {
                Object callback = tool.getClass().getMethod("toolCallback").invoke(tool);
                if (callback instanceof ToolCallback)
                {
                    return new ToolCallback[] { (ToolCallback) callback };
                }
            }
        }
        catch (Exception ignored)
        {
        }
        return new ToolCallback[0];
    }
}
