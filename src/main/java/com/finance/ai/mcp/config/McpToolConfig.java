package com.finance.ai.mcp.config;

import com.finance.ai.mcp.tools.FinanceCalculatorTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider financeToolCallbackProvider(FinanceCalculatorTools financeCalculatorTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(financeCalculatorTools)
                .build();
    }
}