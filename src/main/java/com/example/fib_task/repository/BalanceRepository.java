package com.example.fib_task.repository;

import com.example.fib_task.model.Balance;
import com.example.fib_task.model.Currency;
import com.example.fib_task.model.Note;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class BalanceRepository {

  private static final String CASHIER_FILE_PATH = "src/main/resources/balances.txt";

  private final Map<String, Map<Instant, Balance>> balances = new HashMap<>();

  @PostConstruct
  public void loadBalances() throws IOException {
    Files.readAllLines(Paths.get(CASHIER_FILE_PATH)).stream()
        .forEach(line -> {
          final var parts = line.split(";");
          final var name = parts[0];
          final var balance = parseBalances(parts[1]);
          final var notes = parseNotes(parts[2]);
          final var time = parts[3].equals("INIT") ? Instant.now() : Instant.parse(parts[3]);

          balances.compute(name, (key, existingBalance) -> {
            if (existingBalance == null) {
              final var map = new HashMap<Instant, Balance>();
              map.put(time, new Balance(name, time, balance, notes));
              return map;
            } else {
              existingBalance.put(time, new Balance(name, time, balance, notes));
              return existingBalance;
            }
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
                boolean afterFrom = from.map(start -> !balanceEntry.getKey().isBefore(start))
                    .orElse(true);
                boolean beforeTo = to.map(end -> !balanceEntry.getKey().isAfter(end)).orElse(true);
                return afterFrom && beforeTo;
              }).map(Entry::getValue);
        });
  }

  public Mono<Void> saveBalance(final Balance balance) {
    var newLine = balance.name() + ";" + formatBalances(balance.balances()) + ";" + formatNotes(
        balance.notes()) + ";" + balance.timestamp() + "\n";
    return Mono.fromRunnable(() -> {
      try {
        //will fail if new cashier is added
        this.balances.get(balance.name()).put(balance.timestamp(), balance);
        Files.write(Paths.get(CASHIER_FILE_PATH), newLine.getBytes(), StandardOpenOption.APPEND);
      } catch (IOException e) {
        throw new RuntimeException("Error writing to file", e);
      }
    });
  }

  private Mono<Balance> getLastestBalanceByName(final String name) {
    var cashierBalances = balances.get(name);

    if (cashierBalances != null && !cashierBalances.isEmpty()) {
      Instant latestTimestamp = cashierBalances.keySet().stream()
          .max(Comparator.naturalOrder())
          .orElse(null);

      if (latestTimestamp != null) {
        var latestBalance = cashierBalances.get(latestTimestamp);
        return Mono.just(latestBalance);
      }
    }

    return Mono.empty();
  }

  private Map<Currency, Integer> parseBalances(final String balancesStr) {
    return Arrays.stream(balancesStr.split(","))
        .map(balance -> balance.split("="))
        .collect(Collectors.toMap(
            balance -> Currency.valueOf(balance[0]),
            balance -> Integer.parseInt(balance[1])
        ));
  }

  private Map<Note, Integer> parseNotes(final String notesStr) {
    return Arrays.stream(notesStr.split(","))
        .map(note -> note.split("="))
        .map(noteParts -> {
          var currencyAndValue = noteParts[0].split(" ");
          var currency = Currency.valueOf(currencyAndValue[1]);
          var value = Integer.parseInt(currencyAndValue[0]);

          return new AbstractMap.SimpleEntry<>(new Note(currency, value),
              Integer.parseInt(noteParts[1]));
        })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private String formatBalances(final Map<Currency, Integer> balances) {
    return balances.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining(","));
  }

  private String formatNotes(final Map<Note, Integer> notes) {
    return notes.entrySet().stream()
        .map(entry -> entry.getKey().value() + " " + entry.getKey().currency() + "="
            + entry.getValue())
        .collect(Collectors.joining(","));
  }
}
