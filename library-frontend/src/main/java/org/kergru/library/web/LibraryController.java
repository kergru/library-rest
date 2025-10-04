package org.kergru.library.web;

import org.kergru.library.model.BookDto;
import org.kergru.library.model.UserDto;
import org.kergru.library.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/library/ui")
public class LibraryController {

  private final LibraryService libraryService;

  public LibraryController(LibraryService libraryService) {
    this.libraryService = libraryService;
  }


  @GetMapping("/me")
  public Mono<UserDto> me(@AuthenticationPrincipal Jwt jwt) {
    String username = jwt.getClaimAsString("preferred_username");
    if (username == null) {
      // Fallback to sub claim if preferred_username is not available
      username = jwt.getSubject();
    }
    return libraryService.getUserWithLoans(username)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
  }

  @GetMapping("/books")
  public Flux<BookDto> listAllBooks() {
    return libraryService.getAllBooks();
  }

  @GetMapping("/books/{isbn}")
  public Mono<BookDto> showBook(@PathVariable String isbn) {

    return libraryService.getBookByIsbn(isbn)
       .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")));
  }
}