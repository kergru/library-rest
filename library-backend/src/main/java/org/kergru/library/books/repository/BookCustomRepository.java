package org.kergru.library.books.repository;

import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

public interface BookCustomRepository {

  Mono<Page<BookWithLoanDto>> searchBooks(String searchStr, int page, int size, String sortBy);
}
