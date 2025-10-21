package org.kergru.library.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kergru.library.util.JwtTestUtils.createMockJwt;

import org.junit.jupiter.api.Test;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration test for the {@link BookController}. KeyCloak is mocked using mockJwt(), no KeyCloak container required Library Backend is mocked using WireMock Webclient is
 * configured to use a mock JWT
 */
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8081)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MeControllerIT {
  
  @Autowired
  private WebTestClient webTestClient;

  @Test
  void expectGetMeShouldReturnAuthenticatedUser() {

    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .get()
        .uri("/library/api/me")
        .exchange()
        .expectStatus().isOk()
        .expectBody(UserDto.class)
        .value(user -> {
          assertThat(user).isNotNull();
          assertThat(user.userName()).isEqualTo("demo_user_1");
        });
  }

  @Test
  void expectBorrowBookReturnsLoan() {
    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .post()
        .uri("/library/api/me/borrowBook/success-isbn")
        .exchange()
        .expectStatus().isOk()
        .expectBody(LoanDto.class)
        .value(loan -> {
          assertThat(loan).isNotNull();
          assertThat(loan.book().isbn()).isEqualTo("success-isbn");
        });
  }

  @Test
  void expectBorrowAlreadyBorrowedBookReturnsConflict() {
    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .post()
        .uri("/library/api/me/borrowBook/conflict-isbn")
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void expectReturnBookReturnsOk() {
    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .post()
        .uri("/library/api/me/returnBook/1")
        .exchange()
        .expectStatus().isOk();
  }
}
