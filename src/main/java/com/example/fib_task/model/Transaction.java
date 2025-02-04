package com.example.fib_task.model;

import java.time.Instant;

public record Transaction(
    String cashier,
    Instant timestamp,
    Currency currency,
    CashOperationType type,
    Integer amount
) {

  @Override
  public String toString() {
    return cashier + ';' + timestamp + ";" + currency + ";" + type + ";" + amount +'\n';
  }
}
