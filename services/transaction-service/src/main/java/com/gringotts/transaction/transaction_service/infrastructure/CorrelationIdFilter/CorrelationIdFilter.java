package com.gringotts.transaction.transaction_service.infrastructure.CorrelationIdFilter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements Filter {

    private static final String HEADER = "X-Correlation-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            String correlationId = httpRequest.getHeader(HEADER);

            // 🔹 If upstream didn't send → generate new
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            // 🔹 Store in MDC (available everywhere)
            MDC.put("correlationId", correlationId);

            chain.doFilter(request, response);

        } finally {
            // 🔹 VERY IMPORTANT
            MDC.clear();
        }
    }
}