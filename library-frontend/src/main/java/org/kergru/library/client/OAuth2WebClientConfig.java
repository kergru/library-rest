package org.kergru.library.client;

import org.kergru.library.client.logging.LoggingExchangeFilterFunction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient configuration with OAuth2 and logging interceptors.
 */
@Configuration
public class OAuth2WebClientConfig {

  /**
   * Configures WebClient with OAuth2 and logging interceptors.
   */
  @Bean
  WebClient oauth2WebClient(
      LoggingExchangeFilterFunction loggingInterceptor,
      @Value("${library.backend.baseUrl}") String backendBaseUrl) {

    return WebClient.builder()
        .baseUrl(backendBaseUrl)
        .filter(tokenRelayFilter()) // interceptor for adding access token
        .filter(loggingInterceptor) // interceptor for logging
        .build();
  }

  /**
   * Gets the access token from the security context and adds it to the request headers.
   */
    private ExchangeFilterFunction tokenRelayFilter() {
    return (request, next) -> ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .cast(JwtAuthenticationToken.class)
        .map(auth -> ClientRequest.from(request)
            .headers(h -> h.setBearerAuth(auth.getToken().getTokenValue()))
            .build()
        )
        .defaultIfEmpty(request)
        .flatMap(next::exchange);
  }
}