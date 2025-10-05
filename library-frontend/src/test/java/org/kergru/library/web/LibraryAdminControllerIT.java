package org.kergru.library.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kergru.library.util.TestUtils.getAccessToken;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration test for the {@link LibraryAdminController}.
 * KeyCloak is started as a container
 * Library Backend is mocked using WireMock
 * Webclient is configured to use a mock JWT
 */
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port=8081)
@Import(KeycloakTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LibraryAdminControllerIT {

  @Autowired
  private WebTestClient webTestClient;

  @Test
  void expectListAllUsersWithRoleLibrarianShouldReturnUsers() {
    String token = getAccessToken("librarian", "pwd");

    webTestClient
        .get()
        .uri("/library/ui/admin/users")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> assertThat(body).contains("demo_user_1"))
        .value(body -> assertThat(body).contains("demo_user_2"))
        .value(body -> assertThat(body).contains("demo_user_3"));
  }

  @Test
  void expectListAllUsersWithNotRoleLibrarianShouldReturnForbidden() {
    String token = getAccessToken("demo_user_1", "pwd");

    webTestClient
        .get()
        .uri("/library/ui/admin/users")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  void expectGetUserWithRoleLibrarianShouldReturnUser() {
    String token = getAccessToken("librarian", "pwd");

    webTestClient
        .get()
        .uri("/library/ui/admin/users/demo_user_1")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value(body -> assertThat(body).contains("demo_user_1"));
  }

  @Test
  void expectGetUserWithNotRoleLibrarianShouldReturnForbidden() {
    String token = getAccessToken("demo_user_1", "pwd");

    webTestClient
        .get()
        .uri("/library/ui/admin/users/demo_user_2")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange()
        .expectStatus().isForbidden();
  }
}
