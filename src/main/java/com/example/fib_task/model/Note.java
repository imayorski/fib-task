package com.example.fib_task.model;

public record Note(
    Currency currency,
    Integer value
) {

  @Override
  public String toString() {
    return currency.toString() + value;
  }
}
