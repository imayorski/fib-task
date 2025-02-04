package com.example.fib_task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@SpringBootApplication
public class FibTaskApplication {

  public static void main(String[] args) {
    SpringApplication.run(FibTaskApplication.class, args);
  }

}
