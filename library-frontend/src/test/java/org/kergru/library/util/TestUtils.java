package org.kergru.library.util;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import com.nimbusds.jwt.SignedJWT;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.JwtMutator;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

public class TestUtils {

  // ---- Helper: Wait for JWKS to be available ----
  public static void waitForJwks() {
    WebClient webClient = WebClient.create();
    String jwksUrl = "http://localhost:8085/realms/library/protocol/openid-connect/certs";

    webClient.get()
        .uri(jwksUrl)
        .retrieve()
        .bodyToMono(String.class)
        .retryWhen(Retry.backoff(30, Duration.ofSeconds(1))
            .doBeforeRetry(retrySignal ->
                System.out.println("Waiting for JWKS... (try " + (retrySignal.totalRetries() + 1) + ")"))
            .onRetryExhaustedThrow((spec, signal) ->
                new RuntimeException("JWKS not available after " + signal.totalRetries() + " retries")))
        .doOnSuccess(resp -> System.out.println("JWKS available."))
        .block();
  }

  // ---- Helper: Retrieve access token from Keycloak ----
  public static String getAccessToken(String username, String password) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("client_id", "library-frontend");
    form.add("client_secret", "secret");
    form.add("grant_type", "password");
    form.add("username", username);
    form.add("password", password);

    WebClient tokenClient = WebClient.create();

    return tokenClient.post()
        .uri("http://localhost:8085/realms/library/protocol/openid-connect/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .bodyValue(form)
        .retrieve()
        .bodyToMono(Map.class)
        .map(response -> {
          String token = (String) response.get("access_token");
          System.out.println("Token: " + token);
          try {
            SignedJWT jwt = SignedJWT.parse(token);
            System.out.println(jwt.getJWTClaimsSet().toJSONObject());
          } catch (Exception e) {
            e.printStackTrace();
          }
          return token;
        })
        .block();
  }

  public static JwtMutator createMockJwt(String username) {
    return mockJwt()
        .jwt(jwt -> jwt
            .claim("sub", username)
            .claim("preferred_username", username)
            .claim("scope", "profile email")
            .issuer("http://localhost:8085/realms/library")
        );
  }

  public static JwtMutator createMockJwtWithRoleLibrarian(String username) {
    return mockJwt()
        .jwt(jwt -> jwt
            .claim("sub", username)
            .claim("preferred_username", username)
            .claim("realm_access", Map.of("roles", List.of("LIBRARIAN")))
            .claim("scope", "profile email")
            .issuer("http://localhost:8085/realms/library")
        );
  }
}
