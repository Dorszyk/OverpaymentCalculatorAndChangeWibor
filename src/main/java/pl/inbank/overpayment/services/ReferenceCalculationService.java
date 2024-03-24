package pl.inbank.overpayment.services;

import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.Installment;
import pl.inbank.overpayment.model.InstallmentAmounts;
import pl.inbank.overpayment.model.Reference;

public interface ReferenceCalculationService {

    Reference calculate(InstallmentAmounts installmentAmounts, InputData inputData);

    Reference calculate(InstallmentAmounts installmentAmounts, final InputData inputData, Installment previousInstallment);

}
