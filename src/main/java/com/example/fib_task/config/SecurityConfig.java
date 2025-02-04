package com.example.fib_task.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String API_KEY_HEADER = "FIB-X-AUTH";
    @Value("${security.api.key}")
    private String apiKey;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchange -> exchange
                .anyExchange().permitAll()
            )
            .addFilterBefore(new ApiKeyAuthFilter(), SecurityWebFiltersOrder.FIRST)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .build();
    }

    public class ApiKeyAuthFilter implements WebFilter {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            var request = exchange.getRequest();
            var authHeaders = request.getHeaders().getOrEmpty(API_KEY_HEADER);

            if (authHeaders.isEmpty() || !authHeaders.contains(apiKey)) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.writeWith(Mono.empty());
            }

            return chain.filter(exchange);
        }
    }

}
