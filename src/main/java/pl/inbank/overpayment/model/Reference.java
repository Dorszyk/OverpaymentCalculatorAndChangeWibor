package pl.inbank.overpayment.model;

import lombok.Builder;
import lombok.With;

import java.math.BigDecimal;

@With
public record Reference(BigDecimal referenceAmount, BigDecimal referenceDuration) {

    @Builder
    public Reference {
    }
}
