package com.example.fib_task.exception;

public class InsufficientBalanceException extends RuntimeException{
  public InsufficientBalanceException() {
    super("Insufficient balance to perform withdrawal.");
  }
}
