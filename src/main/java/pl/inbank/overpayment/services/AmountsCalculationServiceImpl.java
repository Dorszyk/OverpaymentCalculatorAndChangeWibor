package pl.inbank.overpayment.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.Installment;
import pl.inbank.overpayment.model.InstallmentAmounts;
import pl.inbank.overpayment.model.Overpayment;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class AmountsCalculationServiceImpl implements AmountsCalculationService {

    private final ConstantAmountsCalculationService constantAmountsCalculationService;

    private final DecreasingAmountsCalculationService decreasingAmountsCalculationService;

    @Override
    public InstallmentAmounts calculate(final InputData inputData, final Overpayment overpayment) {
        return switch (inputData.installmentType()) {
            case CONSTANT -> constantAmountsCalculationService.calculate(inputData, overpayment);
            case DECREASING -> decreasingAmountsCalculationService.calculate(inputData, overpayment);
        };
    }

    @Override
    public InstallmentAmounts calculate(final InputData inputData, final Overpayment overpayment,final BigDecimal residualAmount,final Installment previousInstallment) {
        return switch (inputData.installmentType()) {
            case CONSTANT -> constantAmountsCalculationService.calculate(inputData, overpayment, previousInstallment);
            case DECREASING -> decreasingAmountsCalculationService.calculate(inputData, overpayment, previousInstallment);
        };
    }


}
