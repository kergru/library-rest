package org.kergru.library.client;

import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LibraryBackendClient {

  private final WebClient webClient;

  public LibraryBackendClient(WebClient oauth2WebClient) {
    this.webClient = oauth2WebClient;
  }

  /**
   * Searches books from the backend by given search criteria.
   * Using the token relay pattern.
   */
  public Mono<PageResponseDto<BookDto>> searchBooks(String searchString, int page, int size, String sortBy) {
    return webClient.get()
        .uri(uriBuilder -> {
          var builder = uriBuilder
              .path("/library/api/books")
              .queryParam("page", page)
              .queryParam("size", size)
              .queryParam("sort", sortBy);
          if (searchString != null && !searchString.isEmpty()) {
            builder.queryParam("searchString", searchString);
          }
          return builder.build();
        })
        .retrieve()
        .onStatus(
            status -> status.is4xxClientError() || status.is5xxServerError(),
            ClientResponse::createException
        )
        .bodyToMono(new ParameterizedTypeReference<>() {});
  }

  /**
   * Retrieves a single book by its ISBN from the backend.
   * Using the token relay pattern.
   */
  public Mono<BookDto> getBookByIsbn(String isbn) {
    return webClient.get()
        .uri("/library/api/books/{isbn}", isbn)
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToMono(BookDto.class);
  }

  /**
   * Searches users by given search criteria.
   * Using the token relay pattern.
   */
  public Mono<PageResponseDto<UserDto>> searchUsers(String searchString, int page, int size, String sortBy) {
    return webClient.get()
        .uri(uriBuilder -> {
          var builder = uriBuilder
              .path("/library/api/users")
              .queryParam("page", page)
              .queryParam("size", size)
              .queryParam("sort", sortBy);
          if (searchString != null && !searchString.isEmpty()) {
            builder.queryParam("searchString", searchString);
          }
          return builder.build();
        })
        .retrieve()
        .onStatus(
            status -> status.is4xxClientError() || status.is5xxServerError(),
            ClientResponse::createException
        )
        .bodyToMono(new ParameterizedTypeReference<>() {});
  }

  /**
   * Retrieves a single user by userName from the backend.
   * Using the token relay pattern.
   */
  public Mono<UserDto> getUser(String userName) {
    return webClient.get()
        .uri("/library/api/users/{userName}", userName)
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToMono(UserDto.class);
  }

  /**
   * Retrieves book loans of a user from the backend.
   * Using the token relay pattern.
   */
  public Flux<LoanDto> getBorrowedBooksOfUser(String userName) {
    return webClient.get()
        .uri("/library/api/users/{userName}/loans", userName)
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToFlux(LoanDto.class);
  }
}