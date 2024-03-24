package pl.inbank.overpayment.model;

import lombok.Builder;
import lombok.With;

import java.math.BigDecimal;

@With
public record InstallmentAmounts(
        BigDecimal installmentAmount,
        BigDecimal interestAmount,
        BigDecimal capitalAmount,
        Overpayment overpayment,
        BigDecimal monthlyFee


) {

    @Builder
    public InstallmentAmounts {
    }

}
