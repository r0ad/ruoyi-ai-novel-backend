package com.ruoyi.novel.agent;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 将工作流 Tool 上下文传播到 Spring AI 实际执行工具的线程（如 boundedElastic）。
 */
@Component
public class NovelToolContextPropagator
{
    public Object wrapToolBean(Object target, NovelToolContext.Context ctx)
    {
        if (target == null || ctx == null)
        {
            return target;
        }
        ProxyFactory factory = new ProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAdvice((MethodInterceptor) invocation -> {
            if (invocation.getMethod().getDeclaringClass() == Object.class)
            {
                return invocation.proceed();
            }
            try
            {
                NovelToolContext.apply(ctx);
                return invocation.proceed();
            }
            finally
            {
                NovelToolContext.clearThreadLocal();
            }
        });
        return factory.getProxy();
    }

    public ToolCallback wrapToolCallback(ToolCallback callback, NovelToolContext.Context ctx)
    {
        if (callback == null || ctx == null)
        {
            return callback;
        }
        return new ToolCallback()
        {
            @Override
            public ToolDefinition getToolDefinition()
            {
                return callback.getToolDefinition();
            }

            @Override
            public String call(String toolInput)
            {
                try
                {
                    NovelToolContext.apply(ctx);
                    return callback.call(toolInput);
                }
                finally
                {
                    NovelToolContext.clearThreadLocal();
                }
            }

            @Override
            public String call(String toolInput, @Nullable ToolContext toolContext)
            {
                try
                {
                    NovelToolContext.apply(ctx);
                    return callback.call(toolInput, toolContext);
                }
                finally
                {
                    NovelToolContext.clearThreadLocal();
                }
            }
        };
    }
}
