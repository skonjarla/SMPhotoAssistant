package net.konjarla.smagent.config;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.konjarla.smagent.service.RequestLoggingInterceptor;
import net.konjarla.smugmug.client.OAuth1HttpClient;
import net.konjarla.smugmug.client.api.Users;
import net.konjarla.smugmug.model.SMUser;
import net.konjarla.smugmug.oauth.OAuth1Signature;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class SmugMugConfiguration {
    @Value("${smugmug.consumer.key}")
    @NonNull
    private String consumerKey;

    @Value("${smugmug.consumer.secret}")
    @NonNull
    private String consumerSecret;

    @Value("${smugmug.access.token}")
    @NonNull
    private String accessToken;

    @Value("${smugmug.access.token.secret}")
    @NonNull
    private String accessTokenSecret;

    @Bean
    public OAuth1HttpClient oAuth1HttpClient() {
        return new OAuth1HttpClient.Builder()
                .signatureBuilder(new OAuth1Signature.Builder()
                        .consumerKey(consumerKey)
                        .consumerSecret(consumerSecret)
                        .accessToken(accessToken)
                        .tokenSecret(accessTokenSecret))
                .httpClientBuilder(HttpClientBuilder.create()
                        .addRequestInterceptorFirst(new RequestLoggingInterceptor()))
                .build();
    }

    @Bean
    public SMUser smugMugUser() {
        return Users.getAuthenticatedUser(oAuth1HttpClient());
    }
}
