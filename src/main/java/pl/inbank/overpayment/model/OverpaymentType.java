package pl.inbank.overpayment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OverpaymentType {
    CONSTANT("CONSTANT"),
    DECREASING("DECREASING");

    private final String value;
}
