package pl.inbank.overpayment.services;

import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.Overpayment;

import java.math.BigDecimal;

public interface OverpaymentCalculationService {

    Overpayment calculate(final BigDecimal installmentNumber, final InputData inputData);
}
