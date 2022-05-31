package com.example.cb;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class DelayController {

    private static final String RESILIENCE4J_INSTANCE_NAME = "demo";
    private static final String FALLBACK_METHOD = "fallback";

    @GetMapping(
            value = "/delay/{delay}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE_NAME)
    public Mono<Response<Boolean>> delay(@PathVariable int delay) {
        return Mono.just(toOkResponse())
                .delayElement(Duration.ofSeconds(delay));
    }

    @GetMapping(
            value = "/delay2/{delay}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE_NAME, fallbackMethod = FALLBACK_METHOD)
    public Mono<Response<Boolean>> delay2(@PathVariable int delay) {
        return Mono.just(toOkResponse())
                .delayElement(Duration.ofSeconds(delay));
    }

    public Mono<Response<Boolean>> fallback(Exception ex) {
        return Mono.just(toResponse(HttpStatus.INTERNAL_SERVER_ERROR, Boolean.FALSE))
                .doOnNext(result -> log.warn("fallback executed"));
    }

    private Response<Boolean> toOkResponse() {
        return toResponse(HttpStatus.OK, Boolean.TRUE);
    }

    private Response<Boolean> toResponse(HttpStatus httpStatus, Boolean result) {
        return Response.<Boolean>builder()
                .code(httpStatus.value())
                .status(httpStatus.getReasonPhrase())
                .data(result)
                .build();
    }

}
