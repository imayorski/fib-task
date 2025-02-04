package com.example.fib_task.repository;

import com.example.fib_task.model.Transaction;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class TransactionRepository {

  private static final String TRANSACTIONS_FILE_PATH = "src/main/resources/transactions.txt";

  public Mono<Void> saveTransaction(final
      Transaction transaction) {
    return Mono.fromRunnable(() -> {
      try {
        Files.write(Paths.get(TRANSACTIONS_FILE_PATH),
            transaction.toString().getBytes(), StandardOpenOption.APPEND);
      } catch (IOException e) {
        throw new RuntimeException("Error writing to file", e);
      }
    });
  }
}
