package com.example.fib_task.model;

import java.time.Instant;
import java.util.Map;

public record Balance(
    String name,
    Instant timestamp,
    Map<Currency, Integer> balances,
    Map<Note, Integer> notes
) {

}
