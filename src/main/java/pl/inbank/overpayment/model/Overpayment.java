package pl.inbank.overpayment.model;

import lombok.Builder;
import lombok.With;

import java.math.BigDecimal;

@With
public record Overpayment(BigDecimal amount, BigDecimal provisionAmount) {

    public static final String REDUCE_INSTALLMENT = "REDUCE_INSTALLMENT";

    public static final String REDUCE_PERIOD = "REDUCE_PERIOD";

    @Builder
    public Overpayment {
    }
}
