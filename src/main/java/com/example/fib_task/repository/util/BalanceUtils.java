package com.example.fib_task.repository.util;

import com.example.fib_task.model.Balance;
import com.example.fib_task.model.Currency;
import com.example.fib_task.model.Note;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class BalanceUtils {

  public static Balance fromLine(final String line) {
    final var parts = line.split(";");
    final var name = parts[0];
    final var balance = parseBalances(parts[1]);
    final var notes = parseNotes(parts[2]);
    final var time = parts[3].equals("INIT") ? Instant.now() : Instant.parse(parts[3]);

    return new Balance(name, time, balance, notes);
  }

  public static String toLine(final Balance balance) {
    return balance.name() + ";" + formatBalances(balance.balances()) + ";" + formatNotes(
        balance.notes()) + ";" + balance.timestamp() + "\n";
  }

  private static Map<Currency, Integer> parseBalances(final String balancesStr) {
    return Arrays.stream(balancesStr.split(","))
        .map(balance -> balance.split("="))
        .collect(Collectors.toMap(
            balance -> Currency.valueOf(balance[0]),
            balance -> Integer.parseInt(balance[1])
        ));
  }

  private static Map<Note, Integer> parseNotes(final String notesStr) {
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

  private static String formatBalances(final Map<Currency, Integer> balances) {
    return balances.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining(","));
  }

  private static String formatNotes(final Map<Note, Integer> notes) {
    return notes.entrySet().stream()
        .map(entry -> entry.getKey().value() + " " + entry.getKey().currency() + "="
            + entry.getValue())
        .collect(Collectors.joining(","));
  }
}
