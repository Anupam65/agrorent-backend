package com.agrorent.api.dto;

import com.agrorent.api.model.AvailabilityStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EquipmentDTO {

    @NotBlank(message = "Equipment name is required")
    private String name;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price per day is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price per day must be non-negative")
    private BigDecimal pricePerDay;

    @NotNull(message = "Price per hour is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price per hour must be non-negative")
    private BigDecimal pricePerHour;

    private AvailabilityStatus availabilityStatus;

    private String imageUrl;

    @NotBlank(message = "Location is required")
    private String location;
}
