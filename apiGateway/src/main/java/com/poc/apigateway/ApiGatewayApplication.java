package com.poc.apigateway;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public ApplicationRunner runner(Environment env) {
        return args -> {
            System.out.println("==== GATEWAY ROUTES CHECK ====");
            System.out.println(env.getProperty("spring.cloud.gateway.server.webflux.routes"));
        };
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        System.out.println("==== ROUTES INITIALIZED ====");
        return builder.routes().build();
    }


}

