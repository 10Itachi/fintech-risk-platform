package com.gringotts.gateway_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/service-unavailable")
    public ResponseEntity<Map<String, Object>>
    serviceUnavailable() {

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "message",
                "Service temporarily unavailable"
        );

        response.put(
                "status",
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );

        response.put(
                "retryable",
                true
        );

        response.put(
                "timestamp",
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
