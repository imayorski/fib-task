package com.example.fib_task.config;

import com.example.fib_task.exception.InsufficientBalanceException;
import com.example.fib_task.exception.InsufficientNotesException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice("com.example.fib_task.controller")
public class ExceptionControllerAdvice {

  @ExceptionHandler(value = InsufficientBalanceException.class)
  protected ResponseEntity handleInsufficientBalanceException(
      final InsufficientBalanceException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(value = InsufficientNotesException.class)
  protected ResponseEntity handleInsufficientNotesException(
      final InsufficientNotesException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }
}
