package com.poc.apigateway.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter {

    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().value();
        HttpMethod method = exchange.getRequest().getMethod();

        System.out.println("GATEWAY PATH = " + path);

        // Allow preflight CORS requests
        if (HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(exchange);
        }

        // Allow auth endpoints
        if (path.startsWith("/auth")) {
            return chain.filter(exchange);
        }

        // Token required for others
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing token");
        }

        String token = header.substring(7);

        return webClientBuilder.build()
                .post()
                .uri("lb://AUTH-SERVICE/auth/validate")
                .bodyValue(token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .flatMap(valid -> {
                    if (Boolean.FALSE.equals(valid)) {
                        return unauthorized(exchange, "Invalid token");
                    }
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> unauthorized(exchange, "Auth Service unreachable"));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse()
                .writeWith(
                        Mono.just(
                                exchange.getResponse()
                                        .bufferFactory()
                                        .wrap(msg.getBytes())
                        )
                );
    }
}
