package com.example.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PurchaseRequest {
    @NotNull(message = "productName must not be null")
    @Pattern(regexp = "^[a-zA-Z_ ]*$", message = "Invalid ProductName")
    private String productName;
    @NotNull(message = "Quantity must not be null")
    @Positive(message = "Quantity must not be negative or zero")
    private Integer quantity;
}
