package com.example.fib_task.controller;

import com.example.fib_task.model.CashOperation;
import com.example.fib_task.service.CashBalanceService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1")
@AllArgsConstructor
public class CashOperationController {

  private final CashBalanceService service;

  @PostMapping(path = "/cash-operation", produces = MediaType.APPLICATION_JSON_VALUE)
  Mono<Void> getBalance(final @Valid @RequestBody CashOperation cashOperation) {
    return service.processCashOperation(cashOperation);
  }

}
