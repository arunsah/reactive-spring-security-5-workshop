package com.example.library.server.config;

import com.example.library.server.security.LibraryReactiveUserDetailsService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

/** JWT converter that takes the roles from persistent user roles. */
public class LibraryUserRolesJwtAuthenticationConverter
    implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

  private final LibraryReactiveUserDetailsService libraryReactiveUserDetailsService;

  public LibraryUserRolesJwtAuthenticationConverter(
      LibraryReactiveUserDetailsService libraryReactiveUserDetailsService) {
    this.libraryReactiveUserDetailsService = libraryReactiveUserDetailsService;
  }

  @Override
  public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
    return libraryReactiveUserDetailsService
        .findByUsername(jwt.getSubject())
        .map(u -> new UsernamePasswordAuthenticationToken(u, "n/a", u.getAuthorities()));
  }
}
