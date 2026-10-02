package com.smartcart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventorySettingsForm {

    @NotNull(message = "Reorder level is required")
    @Min(value = 0, message = "Reorder level can't be negative")
    private Integer reorderLevel;

    private Long supplierId;
}
