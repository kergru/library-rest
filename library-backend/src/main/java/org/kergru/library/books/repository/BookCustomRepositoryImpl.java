package org.kergru.library.books.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class BookCustomRepositoryImpl implements BookCustomRepository {

  private final R2dbcEntityTemplate template;

  public BookCustomRepositoryImpl(R2dbcEntityTemplate template) {
    this.template = template;
  }

  @Override
  public Mono<Page<BookWithLoanDto>> searchBooks(String searchString, int page, int size, String sortBy) {
    Criteria criteria = Criteria.empty();

    if (StringUtils.hasText(searchString)) {
      criteria = criteria.and("title").like("%" + searchString + "%");
      criteria = criteria.or("author").like("%" + searchString + "%");
      criteria = criteria.or("isbn").like("%" + searchString + "%");
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
    Query pageQuery  = Query.query(criteria).with(pageable);

    Flux<BookWithLoanDto> dataFlux = template.select(BookWithLoanDto.class)
        .matching(pageQuery)
        .all();

    return Mono.zip(dataFlux.collectList(), countBooks(criteria))
        .map(tuple -> new PageImpl<>(
            tuple.getT1(),
            org.springframework.data.domain.PageRequest.of(page, size),
            tuple.getT2()
        ));
  }

  public Mono<Long> countBooks(Criteria criteria) {
    return template.count(Query.query(criteria), BookEntity.class);
  }
}
