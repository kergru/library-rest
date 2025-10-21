package org.kergru.library.rest;

import org.kergru.library.model.LoanDto;
import org.kergru.library.model.UserDto;
import org.kergru.library.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/library/api/me")
public class MeController {

  private final LibraryService libraryService;

  public MeController(LibraryService libraryService) {
    this.libraryService = libraryService;
  }

  @GetMapping()
  public Mono<UserDto> me(@AuthenticationPrincipal Jwt jwt) {

    String username = jwt.getClaimAsString("preferred_username");
    return libraryService.getUserWithLoans(username)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
  }

  /**
   * Borrows a book to a user.
   */
  @PostMapping("/borrowBook/{isbn}")
  public Mono<LoanDto> borrowBook(@PathVariable String isbn, @AuthenticationPrincipal Jwt jwt) {

    String username = jwt.getClaimAsString("preferred_username");
    return libraryService.borrowBook(isbn, username)
        .onErrorMap(e -> new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
  }

  /**
   * Returns a book to the library.
   */
  @PostMapping("/returnBook/{loanId}")
  public Mono<Void> returnBook(@PathVariable Long loanId, @AuthenticationPrincipal Jwt jwt) {

    String username = jwt.getClaimAsString("preferred_username");
    return libraryService.returnBook(loanId, username);
  }
}
