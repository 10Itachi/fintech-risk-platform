package com.gringotts.userservice.user_service.integration.keycloak;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


    /*Fetch the client details configured in yml & Keycloak */
    @Configuration
    public class keycloakConfig {

        @Value("${keycloak.server-url}")
        private String serverUrl;

        @Value("${keycloak.realm}")
        private String realm;

        @Value("${keycloak.client-id}")
        private String clientId;

        @Value("${keycloak.client-secret}")
        private String clientSecret;

        /*This method sets up credential for the user service to make connection with the keycloak user-service client */
        @Bean
        public Keycloak keycloak() {
            return KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS) // setting credential using clientId and clientSecret from yml
                    .resteasyClient(
                            new ResteasyClientBuilderImpl()
                                    .connectionPoolSize(10)
                                    .connectTimeout(5, TimeUnit.SECONDS)
                                    .readTimeout(10, TimeUnit.SECONDS)
                                    .build()
                    )
                    .build();
        }
    }

