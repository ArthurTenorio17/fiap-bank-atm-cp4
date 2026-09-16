package com.fiap/bank/atm/application;

import java.math.BigDecimal;

public record AccountInfoDTO(
    String id,
    String agency,
    String number,
    BigDecimal balance,
    String status
) {}
