package com.example.fib_task.controller;

import com.example.fib_task.model.Balance;
import com.example.fib_task.service.CashBalanceService;
import java.time.Instant;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Validated
@RequestMapping("/api/v1")
@AllArgsConstructor
public class BalanceController {

  private final CashBalanceService service;

  @GetMapping(path = "/cash-balance", produces = MediaType.APPLICATION_JSON_VALUE)
  Flux<Balance> getBalance(
      @RequestParam(value = "dateFrom", required = false) final Optional<Instant> from,
      @RequestParam(value = "dateTo", required = false) final Optional<Instant> to,
      @RequestParam(value = "cashier", required = false) final Optional<String> cashierName) {
    return service.getBalance(from, to, cashierName);
  }

}
