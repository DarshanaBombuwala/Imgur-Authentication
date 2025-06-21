package com.assignment.assignment.config;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImgurConfig {

    // These values are injected from application.properties
    @Value("${imgur.client-id}")
    private String clientId;

    @Value("${imgur.client-secret}")
    private String clientSecret;

    @Value("${imgur.callback-url}")
    private String callbackUrl;

    /**
     * Configures and returns an OAuth2 service instance for Imgur using ScribeJava.
     */
    @Bean
    public OAuth20Service imgurService() {
        return new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .callback(callbackUrl)
                .build(com.github.scribejava.apis.ImgurApi.instance());
    }
}
