package com.fiap/bank/atm/application;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDTO(
    String id,
    String type,
    BigDecimal amount,
    LocalDateTime createdAt
) {}
