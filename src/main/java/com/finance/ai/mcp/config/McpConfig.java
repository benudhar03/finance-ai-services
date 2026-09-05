package com.finance.ai.mcp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import com.finance.ai.mcp.client.McpClient;

@Configuration
public class McpConfig {
    @Bean
    public McpClient mcpClient() { return new McpClient(); }
}
