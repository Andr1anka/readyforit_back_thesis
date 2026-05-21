package com.andr1anka.readyforit.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopupRequestDTO {

    @NotNull
    @DecimalMin(value = "10.00", message = "Мінімальна сума 10")
    private BigDecimal amount;

    private String currency = "UAH";
}