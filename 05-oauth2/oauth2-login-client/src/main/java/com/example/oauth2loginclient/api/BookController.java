package com.example.oauth2loginclient.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Controller
@RequestMapping("/books")
public class BookController {

  private static final Logger LOGGER = LoggerFactory.getLogger(BookController.class);

  private final WebClient webClient;

  public BookController(WebClient webClient) {
    this.webClient = webClient;
  }

  @GetMapping
  public String books() {
    return "books";
  }

  @ModelAttribute("allBooks")
  Flux<BookResource> allBooks() {
    return webClient
        .get()
        .uri("http://localhost:8080/books")
        .attributes(clientRegistrationId("library-client"))
        .retrieve()
        .onStatus(
            httpStatus -> HttpStatus.FORBIDDEN.value() == httpStatus.value(),
            clientResponse -> Mono.error(new AccessDeniedException("No access for this resource")))
        .onStatus(
            HttpStatus::is5xxServerError,
            clientResponse -> Mono.error(new IllegalStateException("Internal error occurred")))
        .bodyToFlux(BookResource.class);
  }

  @ExceptionHandler(WebClientResponseException.class)
  public ResponseEntity<String> handleWebClientResponseException(WebClientResponseException ex) {
    LOGGER.error(
        "Error from WebClient - Status {}, Body {}",
        ex.getRawStatusCode(),
        ex.getResponseBodyAsString(),
        ex);
    return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getResponseBodyAsString());
  }
}
