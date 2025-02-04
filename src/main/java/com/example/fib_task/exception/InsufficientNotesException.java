package com.example.fib_task.exception;

public class InsufficientNotesException extends RuntimeException{
  public InsufficientNotesException() {
    super("Insufficient notes to perform withdrawal.");
  }
}
