package pl.inbank.overpayment.services;

import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.Installment;
import pl.inbank.overpayment.model.InstallmentAmounts;
import pl.inbank.overpayment.model.Residual;

public interface ResidualCalculationService {

    Residual calculate(InstallmentAmounts installmentAmounts, InputData inputData);

    Residual calculate(InstallmentAmounts installmentAmounts, final InputData inputData, Installment previousInstallment);

}
