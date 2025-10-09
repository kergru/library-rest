package org.kergru.library.web;

import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.kergru.library.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/library/ui/admin")
public class LibraryAdminController {

  private final LibraryService libraryService;

  public LibraryAdminController(LibraryService libraryService) {
    this.libraryService = libraryService;
  }

  @GetMapping("/users")
  public Mono<PageResponseDto<UserDto>> getUsers(
      @RequestParam(required = false) String searchString,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "firstName") String sortBy
  ) {
    return libraryService.getUsers(searchString, page, size, sortBy);
  }

  @GetMapping("/users/{userName}")
  public Mono<UserDto> getUser(@PathVariable String userName) {

    return libraryService.getUserWithLoans(userName)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
  }
}
