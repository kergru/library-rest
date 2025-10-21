package org.kergru.library.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kergru.library.util.JwtTestUtils.createMockJwt;
import static org.kergru.library.util.JwtTestUtils.createMockJwtWithRoleLibrarian;

import org.junit.jupiter.api.Test;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration test for the {@link UserController}. KeyCloak is mocked using mockJwt(), no KeyCloak container required Library Backend is mocked using WireMock Webclient is
 * configured to use a mock JWT
 */
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8081)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIT {

  @Autowired
  private WebTestClient webTestClient;

  @Test
  void expectSearchUsersWithRoleLibrarianShouldReturnUsers() {

    webTestClient
        .mutateWith(createMockJwtWithRoleLibrarian("librarian"))
        .get()
        .uri("/library/api/users")
        .exchange()
        .expectStatus().isOk()
        .expectBody(new ParameterizedTypeReference<PageResponseDto<UserDto>>() {})
        .value(response -> {
          assertThat(response).isNotNull();
          assertThat(response.content()).isNotEmpty();
          assertThat(response.content().getFirst().userName()).isEqualTo("demo_user_1");
        });
  }

  @Test
  void expectSearchUsersWithNotRoleLibrarianShouldReturnForbidden() {

    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .get()
        .uri("/library/api/users") //protected route without role librarian
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  void expectGetUserWithRoleLibrarianShouldReturnUser() {

    webTestClient
        .mutateWith(createMockJwtWithRoleLibrarian("librarian"))
        .get()
        .uri("/library/api/users/demo_user_1") //protected route without role librarian
        .exchange()
        .expectStatus().isOk()
        .expectBody(UserDto.class)
        .value(user -> {
          assertThat(user.userName()).isEqualTo("demo_user_1");
        });
  }

  @Test
  void expectGetUserWithNotRoleLibrarianShouldReturnForbidden() {

    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .get()
        .uri("/library/api/users/demo_user_2")
        .exchange()
        .expectStatus().isForbidden();
  }
}
