package com.teknokote.ess.events.publish.cm;

import com.teknokote.ess.authentification.model.LoginRequest;
import com.teknokote.ess.authentification.model.LoginResponse;
import com.teknokote.ess.core.service.LoginService;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class CMFeignClientConfiguration {

    private final LoginService loginService;
    @Value("${spring.security.oauth2.client.provider.keycloak.username}")
    private String adminLogin;
    @Value("${spring.security.oauth2.client.provider.keycloak.password}")
    private String adminPassword;
    private ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();

    public CMFeignClientConfiguration(LoginService loginService) {
        this.loginService = loginService;
    }

    @Bean
    public RequestInterceptor requestTokenInterceptor() {
        return requestTemplate -> {
            String cachedToken = tokenCache.get("accessToken");
            if (cachedToken != null) {
                requestTemplate.header("Authorization", "Bearer " + cachedToken);
            } else {
                try {
                    // Use adminLogin and adminPassword directly here
                    LoginResponse response = loginService.login(LoginRequest.init(adminLogin, adminPassword, true), false);
                    String token = response.getAccessToken();
                    requestTemplate.header("Authorization", "Bearer " + token);
                    tokenCache.put("accessToken", token);
                } catch (Exception e) {
                    // Handle token retrieval failure with a custom exception
                    log.error("Failed to retrieve access token: " + e.getMessage(), e);
                    throw new TokenRetrievalException("Failed to retrieve access token", e);
                }
            }
        };
    }
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CardManagerErrorDecoder();
    }

    // Define a dedicated exception for token retrieval failures
    public static class TokenRetrievalException extends RuntimeException {
        public TokenRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
