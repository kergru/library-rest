package org.kergru.library.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kergru.library.util.JwtTestUtils.createMockJwt;

import org.junit.jupiter.api.Test;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.PageResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration test for the {@link BookController}. KeyCloak is mocked using mockJwt(), no KeyCloak container required Library Backend is mocked using WireMock Webclient is
 * configured to use a mock JWT
 */
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8081)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerIT {
  
  @Autowired
  private WebTestClient webTestClient;
  
  @Test
  void expectSearchBooksShouldReturnBooks() {

    webTestClient
        .mutateWith(createMockJwt("demo_user_1"))
        .get()
        .uri("/library/api/books?searchString=The Great Gatsby")
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
        .uri("/library/api/books/12345")
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
