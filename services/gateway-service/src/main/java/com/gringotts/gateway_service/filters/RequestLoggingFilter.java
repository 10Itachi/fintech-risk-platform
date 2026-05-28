package com.gringotts.gateway_service.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();

        ServerHttpRequest request = exchange.getRequest();

        String method = request.getMethod().name();

        String path = request.getURI().getPath();

        String correlationId =
                request.getHeaders()
                        .getFirst(CORRELATION_ID_HEADER);

        String clientIp = extractClientIp(request);

        return exchange.getPrincipal()

                .cast(Authentication.class)

                .map(this::extractUsername)

                .defaultIfEmpty("anonymous")

                .flatMap(username ->

                        chain.filter(exchange)

                                .doFinally(signalType -> {

                                    long latency =
                                            System.currentTimeMillis()
                                                    - startTime;

                                    int statusCode =
                                            exchange.getResponse()
                                                    .getStatusCode()
                                                    != null
                                                    ? exchange.getResponse()
                                                    .getStatusCode()
                                                    .value()
                                                    : 500;

                                    Map<String, Object> logMap =
                                            new LinkedHashMap<>();

                                    logMap.put(
                                            "timestamp",
                                            Instant.now().toString()
                                    );

                                    logMap.put("method", method);

                                    logMap.put("path", path);

                                    logMap.put("status", statusCode);

                                    logMap.put("latencyMs", latency);

                                    logMap.put(
                                            "correlationId",
                                            correlationId
                                    );

                                    logMap.put("user", username);

                                    logMap.put("clientIp", clientIp);

                                    log.info(
                                            "API_GATEWAY_REQUEST_LOG {}",
                                            logMap
                                    );
                                })
                );
    }

    @Override
    public int getOrder() {

        return 0;
    }

    private String extractUsername(Authentication authentication) {

        if (authentication instanceof JwtAuthenticationToken jwtAuthToken) {

            Jwt jwt = jwtAuthToken.getToken();

            String preferredUsername =
                    jwt.getClaimAsString("preferred_username");

            return preferredUsername != null
                    ? preferredUsername
                    : authentication.getName();
        }

        return authentication.getName();
    }

    private String extractClientIp(ServerHttpRequest request) {

        String xForwardedFor =
                request.getHeaders()
                        .getFirst("X-Forwarded-For");

        if (xForwardedFor != null
                && !xForwardedFor.isBlank()) {

            return xForwardedFor.split(",")[0];
        }

        InetSocketAddress remoteAddress =
                request.getRemoteAddress();

        return remoteAddress != null
                ? remoteAddress.getAddress().getHostAddress()
                : "unknown";
    }
}