package pl.inbank.overpayment.model;

import lombok.Builder;
import lombok.With;

import java.math.BigDecimal;

@With
public record Residual(BigDecimal residualAmount, BigDecimal residualDuration) {

    @Builder
    public Residual {
    }
}
