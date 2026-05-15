package com.gringotts.risk.risk_decision_service.infrastructure.CorrelationIdFilter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;


/**
 * Servlet Filter responsible for Distributed Tracing.
 * Intercepts every incoming HTTP request to ensure a unique Tracking ID
 * exists in the SLF4J Mapped Diagnostic Context (MDC).
 */
@Component
@Order(1)
public class CorrelationIdFilter implements Filter {

    private static final String HEADER = "X-Correlation-ID";

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        try {
            String correlationId = null;

            if (request instanceof HttpServletRequest httpRequest) {
                correlationId = httpRequest.getHeader(HEADER);
            }

            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Put into MDC
            MDC.put("correlationId", correlationId);

            // VERY IMPORTANT: propagate back to client
            if (response instanceof HttpServletResponse httpResponse) {
                httpResponse.setHeader(HEADER, correlationId);
            }

            chain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }
}
/*internal implementation of doFilter for understanding*/

/*(Entry and Exit) to this service gate */

/*// Inside the Container's hidden implementation
public void doFilter(ServletRequest req, ServletResponse res) {
    if (iterator.hasNext()) {
        Filter nextFilter = iterator.next();
        // This calls YOUR code!
        nextFilter.doFilter(req, res, this);
    } else {
        // No more filters? Finally call the Controller/Servlet
        servlet.service(req, res);
    }
}*/