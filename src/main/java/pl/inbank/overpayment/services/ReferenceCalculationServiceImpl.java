package pl.inbank.overpayment.services;

import org.springframework.stereotype.Service;
import pl.inbank.overpayment.model.*;

import java.math.BigDecimal;

@Service
public class ReferenceCalculationServiceImpl implements ReferenceCalculationService {

    @Override
    public Reference calculate(InstallmentAmounts installmentAmounts, InputData inputData) {
        if (BigDecimal.ZERO.equals(inputData.amount())) {
            return new Reference(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        return new Reference(inputData.amount(), inputData.monthsDuration());
    }

    @Override
    public Reference calculate(InstallmentAmounts installmentAmounts, final InputData inputData, Installment previousInstallment) {
        if (BigDecimal.ZERO.equals(previousInstallment.residual().residualAmount())) {
            return new Reference(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        return switch (inputData.overpaymentReduceWay()) {
            case Overpayment.REDUCE_INSTALLMENT -> reduceInstallmentReference(installmentAmounts, previousInstallment.residual());
            case Overpayment.REDUCE_PERIOD -> new Reference(inputData.amount(), inputData.monthsDuration());
            default -> throw new Exception("Case not handled");
        };

    }

    private Reference reduceInstallmentReference(final InstallmentAmounts installmentAmounts, final Residual previousResidual) {
        if (installmentAmounts.overpayment().amount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal residualAmount = calculateResidualAmount(previousResidual.residualAmount(), installmentAmounts);
            return new Reference(residualAmount, previousResidual.residualDuration().subtract(BigDecimal.ONE));
        }

        return new Reference(previousResidual.residualAmount(), previousResidual.residualDuration());
    }

    private BigDecimal calculateResidualAmount(final BigDecimal residualAmount, final InstallmentAmounts installmentAmounts) {
        return residualAmount
            .subtract(installmentAmounts.capitalAmount())
            .subtract(installmentAmounts.overpayment().amount())
            .max(BigDecimal.ZERO);
    }

}
