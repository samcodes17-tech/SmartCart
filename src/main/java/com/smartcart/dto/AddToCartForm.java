package com.smartcart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartForm {

    @NotNull
    private Long productId;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
