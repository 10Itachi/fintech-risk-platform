package com.gringotts.transaction.transaction_service.security.serviceauth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class ServiceAuthFeignInterceptor implements RequestInterceptor {

    private final ServiceTokenGenerator serviceTokenGenerator;

    public ServiceAuthFeignInterceptor(ServiceTokenGenerator serviceTokenGenerator) {
        this.serviceTokenGenerator = serviceTokenGenerator;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(
                "Authorization",
                "Bearer "+serviceTokenGenerator.generateServiceToken()
        );
    }
}
