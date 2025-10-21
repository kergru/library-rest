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

/**
 * OAuth2 protected client for library backend using the token relay pattern.
 * Token is relayed from the frontend to the backend. Token will be retrieved from will be automatically
 * added to the request in the tokenRelayFilter method in OAuth2WebClientConfig.
 *
 * @see OAuth2WebClientConfig
 */
@Service
public class LibraryBackendClient {

  private final WebClient webClient;

  public LibraryBackendClient(WebClient oauth2WebClient) {
    this.webClient = oauth2WebClient;
  }

  /**
   * Searches books from the backend using pagination.
   */
  public Mono<PageResponseDto<BookDto>> searchBooks(String searchString, int page, int size, String sortBy) {
    return webClient.get()
        .uri(uriBuilder -> {
          var builder = uriBuilder
              .path("/library/books")
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
   */
  public Mono<BookDto> getBookByIsbn(String isbn) {
    return webClient.get()
        .uri("/library/books/{isbn}", isbn)
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToMono(BookDto.class);
  }

  /**
   * Searches users from the backend using pagination.
   */
  public Mono<PageResponseDto<UserDto>> searchUsers(String searchString, int page, int size, String sortBy) {
    return webClient.get()
        .uri(uriBuilder -> {
          var builder = uriBuilder
              .path("/library/users")
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
   */
  public Mono<UserDto> getUser(String userName) {
    return webClient.get()
        .uri("/library/users/{userName}", userName)
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToMono(UserDto.class);
  }

  /**
   * Retrieves book loans of a user from the backend.
   */
  public Flux<LoanDto> getBorrowedBooksOfUser(String userName) {
    return webClient.get()
        .uri("/library/users/{userName}/loans", userName)
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToFlux(LoanDto.class);
  }

  public Mono<LoanDto> borrowBook(String isbn, String userName) {
    return webClient.post()
        .uri("/library/users/{userName}/loans", userName)
        .body(Mono.just(isbn), String.class)
        .retrieve()
        .onStatus(s -> s.value() == 409, resp -> Mono.error(new BookAlreadyBorrowedException(isbn)))
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToMono(LoanDto.class);
  }

  public Mono<Void> returnBook(Long loanId, String userName) {
    return webClient
        .delete()
        .uri("/library/users/{userName}/loans/{loanId}", userName, loanId)
        .retrieve()
        .onStatus(s -> s.value() == 404, resp -> reactor.core.publisher.Mono.empty())
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            ClientResponse::createException)
        .bodyToMono(Void.class);
  }

  public static class BookAlreadyBorrowedException extends RuntimeException {
    public BookAlreadyBorrowedException(String isbn) {
      super("Book with isbn " + isbn + " is already borrowed");
    }
  }
}
