package pl.inbank.overpayment.services;

import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.Installment;
import pl.inbank.overpayment.model.TimePoint;

import java.math.BigDecimal;

public interface TimePointCalculationService {

    TimePoint calculate(final BigDecimal installmentNumber, final InputData inputData);

    TimePoint calculate(BigDecimal installmentNumber, Installment previousInstallment);

}
