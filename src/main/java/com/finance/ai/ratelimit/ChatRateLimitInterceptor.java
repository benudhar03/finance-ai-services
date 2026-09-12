package com.finance.ai.ratelimit;

import com.finance.ai.exception.RateLimitExceededException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ChatRateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterRegistry rateLimiterRegistry;
    private final RateLimiterConfig chatClientConfig;
    private final RateLimiterConfig chatAgentClientConfig;

    public ChatRateLimitInterceptor(RateLimiterRegistry rateLimiterRegistry,
                                    @Qualifier("chatClientRateLimiterConfig") RateLimiterConfig chatClientConfig,
                                    @Qualifier("chatAgentClientRateLimiterConfig") RateLimiterConfig chatAgentClientConfig) {
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.chatClientConfig = chatClientConfig;
        this.chatAgentClientConfig = chatAgentClientConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        boolean isAgent = request.getRequestURI().contains("/agent");
        RateLimiterConfig baseConfig = isAgent ? chatAgentClientConfig : chatClientConfig;
        String prefix = isAgent ? "chatAgentClient" : "chatClient";
        String clientKey = resolveClientKey(request);
        String limiterName = prefix + ":" + clientKey;

        RateLimiter limiter = rateLimiterRegistry.rateLimiter(limiterName, () -> baseConfig);

        if (!limiter.acquirePermission()) {
            throw new RateLimitExceededException("Too many requests. Please slow down and try again shortly.");
        }
        return true;
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}