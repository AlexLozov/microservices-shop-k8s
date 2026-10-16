package lab.store.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record NewOrderRequest(@NotBlank String productId, @Min(1) int quantity) { }
