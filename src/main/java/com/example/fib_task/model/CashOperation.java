package com.example.fib_task.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CashOperation(
    @NotBlank
    String cashier,
    @NotNull
    CashOperationType operationType,
    @NotNull
    Currency currency,
    @NotNull
    Integer amount,
    @NotEmpty
    List<CashOperationNotes> notes
) {

}
