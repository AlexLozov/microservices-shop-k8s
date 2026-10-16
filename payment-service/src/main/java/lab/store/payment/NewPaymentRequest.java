package lab.store.payment;

import jakarta.validation.constraints.NotBlank;

public record NewPaymentRequest(@NotBlank String orderId) { }
