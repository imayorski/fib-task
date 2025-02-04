package com.example.fib_task.service;

import com.example.fib_task.exception.InsufficientBalanceException;
import com.example.fib_task.exception.InsufficientNotesException;
import com.example.fib_task.model.Balance;
import com.example.fib_task.model.CashOperation;
import com.example.fib_task.model.CashOperationType;
import com.example.fib_task.model.Note;
import com.example.fib_task.model.Transaction;
import com.example.fib_task.repository.BalanceRepository;
import com.example.fib_task.repository.TransactionRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

@Service
@AllArgsConstructor
public class CashBalanceService {

  private final BalanceRepository balanceRepository;
  private final TransactionRepository transactionRepository;

  public Flux<Balance> getBalance(final Optional<Instant> from,
      final Optional<Instant> to ,
      final Optional<String> name) {
    //not sure i understand does the default one return the current balance of the
    //cashier or all balance change events?
    // ..anyway now it returns the current if no data is specified
    if(from.isPresent() || to.isPresent()) {
      return balanceRepository.getAll(from, to, name);
    }

    return balanceRepository.getLatestBalance(name);
  }

  public Mono<Void> processCashOperation(final CashOperation cashOperation) {
    return balanceRepository.getLatestBalance(Optional.of(cashOperation.cashier()))
        .<Balance>handle((balance, sink) -> validateAvailability(balance, sink, cashOperation))
        .map(balance -> prepareNewBalance(balance, cashOperation))
        .flatMap(balanceRepository::saveBalance)
        .then(transactionRepository.saveTransaction(createTransaction(cashOperation)));
  }

  private Transaction createTransaction(final CashOperation cashOperation) {
    return new Transaction(cashOperation.cashier(),
        Instant.now(),
        cashOperation.currency(),
        cashOperation.operationType(),
        cashOperation.amount());
  }
  private void validateAvailability(final Balance balance,
      final SynchronousSink<Balance> sink,
      final CashOperation cashOperation) {
    if (CashOperationType.WITHDRAWAL.equals(cashOperation.operationType())) {
      if (!cashierHasEnoughBalance(balance, cashOperation)) {
        sink.error(new InsufficientBalanceException());
        return;
      }

      if (!cashierHasEnoughNotes(balance, cashOperation)) {
        sink.error(new InsufficientNotesException());
        return;
      }
    }
    sink.next(balance);
  }

  private boolean cashierHasEnoughNotes(final Balance balance,
      final CashOperation cashOperation) {
    return cashOperation.notes().stream()
            .map(n -> n.amount() - balance.notes().get(new Note(cashOperation.currency(), n.value())))
            .anyMatch(i -> i < 0);
  }

  private boolean cashierHasEnoughBalance(final Balance balance,
      final CashOperation cashOperation) {
    return cashOperation.amount() < balance.balances().get(cashOperation.currency());
  }

  private Balance prepareNewBalance(final Balance balance,
      final CashOperation cashOperation) {
    final var updatedBalances = new HashMap<>(balance.balances());
    final var updatedNotes = new HashMap<>(balance.notes());
    final var factor = CashOperationType.DEPOSIT.equals(cashOperation.operationType()) ? 1 : -1;

    cashOperation.notes().forEach(note -> {
      final var n = new Note(cashOperation.currency(), note.value());
      updatedNotes.put(n, updatedNotes.getOrDefault(n, 0) + factor * note.amount());
    });

    updatedBalances.put(cashOperation.currency(),
        updatedBalances.getOrDefault(cashOperation.currency(), 0) + factor * cashOperation.amount());

    return new Balance(balance.name(), Instant.now(), updatedBalances, updatedNotes);
  }

}
