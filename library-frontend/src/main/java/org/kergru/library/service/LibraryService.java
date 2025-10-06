package org.kergru.library.service;

import org.kergru.library.client.LibraryBackendClient;
import org.kergru.library.model.BookDto;
import org.kergru.library.model.LoanDto;
import org.kergru.library.model.UserDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LibraryService {

  private final LibraryBackendClient backendClient;

  public LibraryService(LibraryBackendClient oauth2WebClient) {
    this.backendClient = oauth2WebClient;
  }

  /**
   * Retrieves all books from the backend. Using the token relay pattern.
   */
  public Flux<BookDto> getAllBooks() {
    return backendClient.getAllBooks();
  }

  /**
   * Retrieves a single book by its ISBN from the backend. Using the token relay pattern.
   */
  public Mono<BookDto> getBookByIsbn(String isbn) {
    return backendClient.getBookByIsbn(isbn);
  }

  /**
   * Retrieves all users. Using the token relay pattern.
   */
  public Flux<UserDto> getAllUsers() {
    return backendClient.getAllUsers();
  }

  /**
   * Retrieves a single user with his loans. Using the token relay pattern.
   */
  public Mono<UserDto> getUserWithLoans(String userName) {
    return getUser(userName)
        .flatMap(user -> {
              System.out.println("user: " + user);
              return getBorrowedBooksOfUser(userName)
                  .collectList()
                  .map(loans -> new UserDto(
                      user.userName(),
                      user.firstName(),
                      user.lastName(),
                      user.email(),
                      loans
                  ));
            }
        )
        .map(u -> {
          System.out.println(u);
          return u;
        });
  }

  /**
   * Retrieves a single user by userName. Using the token relay pattern.
   */
  public Mono<UserDto> getUser(String userName) {
    return backendClient.getUser(userName);
  }

  /**
   * Retrieves borrowed books by user Using the token relay pattern. Endpoint is only available for librarians or the user himself.
   */
  public Flux<LoanDto> getBorrowedBooksOfUser(String userId) {
    return backendClient.getBorrowedBooksOfUser(userId);
  }
}