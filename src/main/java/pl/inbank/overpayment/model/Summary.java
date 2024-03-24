package pl.inbank.overpayment.model;

import lombok.Builder;
import lombok.With;

import java.math.BigDecimal;

@With
public record Summary(
    BigDecimal interestSum,
    BigDecimal overpaymentProvisionSum,
    BigDecimal totalLostSum,
    BigDecimal totalCapital,
    BigDecimal totalInterestAndCapital,

    BigDecimal totalAdministrationFee,

    BigDecimal totalAmountPaid
) {

    @Builder
    public Summary {
    }

}
