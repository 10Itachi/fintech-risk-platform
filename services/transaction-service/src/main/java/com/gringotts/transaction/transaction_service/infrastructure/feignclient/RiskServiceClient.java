package com.gringotts.transaction.transaction_service.infrastructure.feignclient;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.transaction.transaction_service.infrastructure.keycloak.ServiceAuthFeignInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/*this establish a connection between risk-decision-service with feign*/
@FeignClient(
        name = "risk-decision-service",
        url ="${risk.service.url}",
        configuration = {
                FeignConfig.class,
                FeignCorrelationConfig.class,
                ServiceAuthFeignInterceptor.class
        }

        /*uses okhhtp dependencies which handles
        * ✔ TCP connection reuse
        * ✔ Keep-alive
        * ✔ Thread efficiency
        * ✔ Socket management
        * ✔ HTTP optimization*
        without this bottle neck in feign connection for larger volumes as it creates one connection per request
         */



)
public interface RiskServiceClient {
    @PostMapping("/risk/evaluate")
    RiskDecisionResponse evaluateRisk(@RequestBody RiskDecisionRequest riskDecisionRequest);
}
