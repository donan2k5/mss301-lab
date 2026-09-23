package com.fudn.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderRequest(
        Long id,
        @NotBlank(message = "skuCode is required") String skuCode,
        @NotNull(message = "price is required") @Positive(message = "price must be positive") BigDecimal price,
        @NotNull(message = "quantity is required") @Positive(message = "quantity must be positive") Integer quantity
) {
}
