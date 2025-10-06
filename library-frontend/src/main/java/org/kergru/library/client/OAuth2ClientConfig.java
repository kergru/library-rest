package org.kergru.library.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for OAuth2 client, appends JWT token to Authorization header. Used for WebClient to call backend services and when the token is only passed from iE external SPA.
 * No token refresh is required here, handled by external SPA.
 */
@Configuration
public class OAuth2ClientConfig {

  private static final Logger logger = LoggerFactory.getLogger(OAuth2ClientConfig.class);

  /**
   * Creates a WebClient that automatically adds the JWT token to the Authorization header.
   */
  @Bean
  public WebClient backendWebClient(@Value("${library.backend.baseUrl}") String backendBaseUrl) {
    return WebClient.builder()
        .baseUrl(backendBaseUrl)
        .filter(tokenRelayFilter())   // setzt Token
        .filter(loggingFilter())      // loggt Request + Token
        .build();
  }

  /**
   * Token aus SecurityContext holen und ins Authorization-Header setzen.
   */
  private ExchangeFilterFunction tokenRelayFilter() {
    return (request, next) -> ReactiveSecurityContextHolder.getContext()
        .map(ctx -> ctx.getAuthentication())
        .cast(JwtAuthenticationToken.class)
        .map(auth -> ClientRequest.from(request)
            .headers(h -> h.setBearerAuth(auth.getToken().getTokenValue()))
            .build()
        )
        .defaultIfEmpty(request)
        .flatMap(next::exchange);
  }

  /**
   * Jeden ausgehenden Request + Token loggen.
   */
  private ExchangeFilterFunction loggingFilter() {
    return ExchangeFilterFunction.ofRequestProcessor(request -> {
      String token = request.headers().getFirst("Authorization");
      System.out.println("Outgoing request: " + request.method() + " " + request.url());
      if (token != null) {
        System.out.println("Authorization: " + token);
      } else {
        System.out.println("No Authorization header present");
      }
      return reactor.core.publisher.Mono.just(request);
    });
  }
}
