package com.example.fib_task.repository;

import com.example.fib_task.model.Balance;
import com.example.fib_task.repository.util.BalanceUtils;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class BalanceRepository {

  private static final String CASHIER_FILE_PATH = "src/main/resources/balances.txt";

  private final Map<String, Map<Instant, Balance>> balances = new HashMap<>();

  @PostConstruct
  public void loadBalances() throws IOException {
    Files.readAllLines(Paths.get(CASHIER_FILE_PATH)).stream()
        .map(BalanceUtils::fromLine)
        .forEach(balance -> {
          balances.compute(balance.name(), (key, existingBalance) -> {
            var map = Objects.isNull(existingBalance) ?
                new HashMap<Instant, Balance>() : existingBalance;
            map.put(balance.timestamp(), balance);
            return map;
          });
        });
  }

  public Flux<Balance> getLatestBalance(final Optional<String> name) {
    return Flux.fromIterable(balances.keySet())
        .filter(key -> !name.isPresent() || name.get().equals(key))
        .flatMap(this::getLastestBalanceByName);
  }

  public Flux<Balance> getAll(final Optional<Instant> from,
      final Optional<Instant> to, final Optional<String> name) {
    return Flux.fromIterable(balances.entrySet())
        .flatMap(entry -> {
          if (name.isPresent() && !entry.getKey().equals(name.get())) {
            return Flux.empty();
          }
          return Flux.fromIterable(entry.getValue().entrySet())
              .filter(balanceEntry -> {
                var afterFrom = from.map(start -> !balanceEntry.getKey().isBefore(start))
                    .orElse(true);
                var beforeTo = to.map(end -> !balanceEntry.getKey().isAfter(end)).orElse(true);
                return afterFrom && beforeTo;
              }).map(Entry::getValue);
        });
  }

  public Mono<Void> saveBalance(final Balance balance) {
    return Mono.fromRunnable(() -> {
      try {
        this.balances.get(balance.name()).put(balance.timestamp(), balance);
        Files.write(Paths.get(CASHIER_FILE_PATH),
            BalanceUtils.toLine(balance).getBytes(), StandardOpenOption.APPEND);
      } catch (IOException e) {
        throw new RuntimeException("Error writing to file", e);
      }
    });
  }

  private Mono<Balance> getLastestBalanceByName(final String name) {
    var cashierBalances = balances.get(name);

    if (!CollectionUtils.isEmpty(cashierBalances)) {
      var latestTimestamp = cashierBalances.keySet().stream()
          .max(Comparator.naturalOrder())
          .orElse(null);

      if (Objects.nonNull(latestTimestamp)) {
        var latestBalance = cashierBalances.get(latestTimestamp);
        return Mono.just(latestBalance);
      }
    }

    return Mono.empty();
  }
}
