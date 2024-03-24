package pl.inbank.overpayment.model;

import lombok.Builder;
import lombok.With;

import java.math.BigDecimal;

@With
public record Installment(
        BigDecimal installmentNumber,
        TimePoint timePoint,
        InstallmentAmounts installmentAmounts,
        Residual residual,
        Reference reference

) {

    @Builder
    public Installment {
    }

}
