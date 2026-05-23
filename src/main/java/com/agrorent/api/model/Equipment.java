package com.agrorent.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Owner is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User owner;

    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category category;

    @NotBlank(message = "Equipment name is required")
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Description is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @NotNull(message = "Price per day is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price per day must be non-negative")
    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @NotNull(message = "Price per hour is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price per hour must be non-negative")
    @Column(name = "price_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @NotNull(message = "Availability status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false)
    @Builder.Default
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;

    @Column(name = "image_url")
    private String imageUrl;

    @NotBlank(message = "Location is required")
    @Column(nullable = false, length = 150)
    private String location;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
