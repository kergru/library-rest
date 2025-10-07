package org.kergru.library.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.kergru.library.security.logging.JwtLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class OAuth2SecurityConfig {

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http,
      JwtLoggingFilter jwtLoggingFilter) {

    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable) // CSRF protection is not needed for a resource server, token secured
        .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS is needed for the frontend to call the backend
        .addFilterAfter(jwtLoggingFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/library/ui/admin/**").hasAuthority("ROLE_LIBRARIAN")
            .anyExchange().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(
                new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter()))
            )
        )
        .build();
  }

  private CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:4200")); // Library client SPA
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setExposedHeaders(List.of("Authorization"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    // Standard-Konverter für Scopes
    JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
    scopesConverter.setAuthorityPrefix("SCOPE_"); // Optional: Authority-Prefix für Scopes

    // Setze den Converter für Scopes
    converter.setJwtGrantedAuthoritiesConverter(scopesConverter);

    // Füge die Logik für realm_access-Rollen hinzu
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
      Collection<GrantedAuthority> authorities = new ArrayList<>(scopesConverter.convert(jwt));

      // Holen Sie sich das realm_access Claim und die Rollen
      Map<String, Object> realmAccess = jwt.getClaim("realm_access");
      if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
      }

      return authorities;
    });

    return converter;
  }
}
