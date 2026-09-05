package com.finance.ai.mcp.service;

import org.springframework.stereotype.Service;
import com.finance.ai.mcp.client.McpClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class McpService {
    private final McpClient client;

    public String proxy(String req) { return client.call(req); }
}
