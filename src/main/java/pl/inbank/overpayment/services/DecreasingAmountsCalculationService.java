package pl.inbank.overpayment.services;

import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.Installment;
import pl.inbank.overpayment.model.InstallmentAmounts;
import pl.inbank.overpayment.model.Overpayment;

public interface DecreasingAmountsCalculationService {

    InstallmentAmounts calculate(InputData inputData, Overpayment overpayment);

    InstallmentAmounts calculate(InputData inputData, Overpayment overpayment, Installment previousInstallment);
}
