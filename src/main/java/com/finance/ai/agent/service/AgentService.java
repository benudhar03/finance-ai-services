package com.finance.ai.agent.service;

import org.springframework.stereotype.Service;
import com.finance.ai.agent.workflow.AgentWorkflow;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentWorkflow workflow;

    public void start() { workflow.run(); }
}
