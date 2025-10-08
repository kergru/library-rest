package org.kergru.library;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class LibraryFrontendApplicationTests {

  @MockitoBean
  private JwtDecoder jwtDecoder;

  @Test
  void contextLoads() {
  }
}
