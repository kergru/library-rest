package org.kergru.library.rest;

import org.kergru.library.model.BookDto;
import org.kergru.library.model.PageResponseDto;
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
@RequestMapping("/library/api/books")
public class BookController {

  private final LibraryService libraryService;

  public BookController(LibraryService libraryService) {
    this.libraryService = libraryService;
  }

  @GetMapping()
  public Mono<PageResponseDto<BookDto>> searchBooks(
      @RequestParam(required = false) String searchString,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "title") String sortBy
  ) {
    return libraryService.searchBooks(searchString, page, size, sortBy);
  }

  @GetMapping("/{isbn}")
  public Mono<BookDto> getBook(@PathVariable String isbn) {

    return libraryService.getBookByIsbn(isbn)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")));
  }
}
