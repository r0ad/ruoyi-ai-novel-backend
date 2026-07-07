package com.ruoyi.novel.agent.capability;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.novel.agent.NovelToolContext;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;

@Component
public class NovelAgentCapabilityRegistry implements InitializingBean
{
    public static final String TODO_WRITE = "agent.todo.write";

    public static final String ASK_USER = "agent.ask_user";

    public static final String SKILLS = "agent.skills";

    public static final String MCP = "mcp.external";

    private static final Logger log = LoggerFactory.getLogger(NovelAgentCapabilityRegistry.class);

    private final Map<String, NovelAgentCapability> capabilities =
        new LinkedHashMap<String, NovelAgentCapability>();

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    @Override
    public void afterPropertiesSet()
    {
        registerSpringAiAgentUtils();
        registerPlaceholder(MCP, "MCP client adapter not configured");
        logCapabilities();
    }

    public List<NovelAgentCapability> listCapabilities()
    {
        return Collections.unmodifiableList(new ArrayList<NovelAgentCapability>(capabilities.values()));
    }

    public List<ToolCallback> listAvailableToolCallbacks(List<String> names)
    {
        List<ToolCallback> callbacks = new ArrayList<ToolCallback>();
        if (names == null)
        {
            return callbacks;
        }
        for (String name : names)
        {
            NovelAgentCapability capability = capabilities.get(name);
            if (capability != null && capability.isAvailable() && capability.getToolCallback() != null)
            {
                callbacks.add(capability.getToolCallback());
            }
        }
        return callbacks;
    }

    public ToolCallback getToolCallback(String name)
    {
        NovelAgentCapability capability = capabilities.get(name);
        if (capability == null || !capability.isAvailable())
        {
            return null;
        }
        return capability.getToolCallback();
    }

    public Object getTool(String name)
    {
        NovelAgentCapability capability = capabilities.get(name);
        if (capability == null || !capability.isAvailable())
        {
            return null;
        }
        return capability.getTool();
    }

    private void registerSpringAiAgentUtils()
    {
        registerToolClass(TODO_WRITE, "org.springaicommunity.agent.tools.TodoWriteTool");
        registerToolClass(ASK_USER, "org.springaicommunity.agent.tools.AskUserQuestionTool");
        registerToolClass(SKILLS, "org.springaicommunity.agent.tools.SkillsTool");
    }

    private void registerToolClass(String name, String className)
    {
        try
        {
            Class<?> toolClass = Class.forName(className);
            Object tool = buildTool(toolClass);
            Object callbackOrTool = asToolCallback(tool);
            if (callbackOrTool == null)
            {
                callbackOrTool = tool;
            }
            if (callbackOrTool != null)
            {
                capabilities.put(name, NovelAgentCapability.available(name, callbackOrTool, className));
                return;
            }
            capabilities.put(name, NovelAgentCapability.unavailable(name, className + " loaded but did not build"));
        }
        catch (ClassNotFoundException ex)
        {
            capabilities.put(name, NovelAgentCapability.unavailable(name, className + " not found"));
        }
        catch (Exception ex)
        {
            capabilities.put(name, NovelAgentCapability.unavailable(name, ex.getMessage()));
        }
    }

    private Object buildTool(Class<?> toolClass) throws Exception
    {
        try
        {
            Method builderMethod = toolClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            configureBuilder(toolClass, builder);
            return builder.getClass().getMethod("build").invoke(builder);
        }
        catch (NoSuchMethodException ignored)
        {
            return toolClass.getDeclaredConstructor().newInstance();
        }
    }

    private void configureBuilder(Class<?> toolClass, Object builder) throws Exception
    {
        if ("org.springaicommunity.agent.tools.TodoWriteTool".equals(toolClass.getName()))
        {
            Class<?> handlerClass =
                Class.forName("org.springaicommunity.agent.tools.TodoWriteTool$TodoEventHandler");
            Object handler = Proxy.newProxyInstance(handlerClass.getClassLoader(), new Class<?>[] { handlerClass },
                todoEventHandler());
            builder.getClass().getMethod("todoEventHandler", handlerClass).invoke(builder, handler);
        }
    }

    private InvocationHandler todoEventHandler()
    {
        return (proxy, method, args) -> {
            if (!"handle".equals(method.getName()) || args == null || args.length == 0)
            {
                return null;
            }
            NovelToolContext.Context ctx = NovelToolContext.get();
            if (ctx != null)
            {
                workflowEventPublisher.publish(ctx.runId, ctx.stepId,
                    NovelWorkflowEventType.TODO_UPDATE.getCode(),
                    JSON.parseObject("{\"todos\":" + JSON.toJSONString(args[0]) + "}"));
            }
            return null;
        };
    }

    private ToolCallback asToolCallback(Object tool) throws Exception
    {
        if (tool instanceof ToolCallback)
        {
            return (ToolCallback) tool;
        }
        if (tool == null)
        {
            return null;
        }
        try
        {
            Method callbackMethod = tool.getClass().getMethod("toolCallback");
            Object callback = callbackMethod.invoke(tool);
            return callback instanceof ToolCallback ? (ToolCallback) callback : null;
        }
        catch (NoSuchMethodException ignored)
        {
            return null;
        }
    }

    private void registerPlaceholder(String name, String detail)
    {
        capabilities.put(name, NovelAgentCapability.unavailable(name, detail));
    }

    private void logCapabilities()
    {
        for (NovelAgentCapability capability : capabilities.values())
        {
            log.info("Novel agent capability {} available={} detail={}", capability.getName(),
                capability.isAvailable(), capability.getDetail());
        }
    }
}
