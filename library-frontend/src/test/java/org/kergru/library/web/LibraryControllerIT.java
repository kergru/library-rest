package org.kergru.library.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kergru.library.util.JwtTestUtils.createMockJwt;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration test for the {@link LibraryController}. KeyCloak is mocked using mockJwt(), no KeyCloak container required Library Backend is mocked using WireMock Webclient is
 * configured to use a mock JWT
 */
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8081)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LibraryControllerIT {

  @Autowired
  private WebTestClient webTestClient;

  @Test
  void expectGetMeShouldReturnAuthenticatedUser() {

    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .get()
        .uri("/library/ui/me")
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
        .uri("/library/ui/me/borrowBook/success-isbn")
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
        .uri("/library/ui/me/borrowBook/conflict-isbn")
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void expectReturnBookReturnsOk() {
    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .post()
        .uri("/library/ui/me/returnBook/1")
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  void expectSearchBooksShouldReturnBooks() {

    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .get()
        .uri("/library/ui/books?searchString=The Great Gatsby")
        .exchange()
        .expectStatus().isOk()
        .expectBody(new ParameterizedTypeReference<PageResponseDto<BookDto>>() {})
        .value(response -> {
          assertThat(response).isNotNull();
          assertThat(response.content()).hasSize(2);
          assertThat(response.content().getFirst().title()).isEqualTo("The Great Gatsby");
          assertThat(response.content().getFirst().isbn()).isEqualTo("12345");
        });
  }

  @Test
  void expectGetBookByIsbnShouldReturnBook() {

    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .get()
        .uri("/library/ui/books/12345")
        .exchange()
        .expectStatus().isOk()
        .expectBody(BookDto.class)
        .value(book -> {
          assertThat(book).isNotNull();
          assertThat(book.title()).isEqualTo("The Great Gatsby");
          assertThat(book.isbn()).isEqualTo("12345");
        });
  }
}
