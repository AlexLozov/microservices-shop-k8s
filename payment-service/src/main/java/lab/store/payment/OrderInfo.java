package lab.store.payment;

import java.math.BigDecimal;

public record OrderInfo(String userId, BigDecimal total, String status) { }
