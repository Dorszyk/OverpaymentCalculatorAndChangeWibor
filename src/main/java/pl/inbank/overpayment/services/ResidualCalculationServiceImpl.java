package pl.inbank.overpayment.services;

import org.springframework.stereotype.Service;
import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.Installment;
import pl.inbank.overpayment.model.InstallmentAmounts;
import pl.inbank.overpayment.model.Residual;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ResidualCalculationServiceImpl implements ResidualCalculationService {

    @Override
    public Residual calculate(InstallmentAmounts installmentAmounts, InputData inputData) {
        if (BigDecimal.ZERO.equals(inputData.amount())) {
            return new Residual(BigDecimal.ZERO, BigDecimal.ZERO);
        } else {
            BigDecimal residualAmount = calculateResidualAmount(inputData.amount(), installmentAmounts);
            BigDecimal residualDuration = calculateResidualDuration(inputData, residualAmount, inputData.monthsDuration(), installmentAmounts);
            return new Residual(residualAmount, residualDuration);
        }
    }

    @Override
    public Residual calculate(InstallmentAmounts installmentAmounts, final InputData inputData, Installment previousInstallment) {
        BigDecimal previousResidualAmount = previousInstallment.residual().residualAmount();
        BigDecimal previousResidualDuration = previousInstallment.residual().residualDuration();

        if (BigDecimal.ZERO.equals(previousResidualAmount)) {
            return new Residual(BigDecimal.ZERO, BigDecimal.ZERO);
        } else {
            BigDecimal residualAmount = calculateResidualAmount(previousResidualAmount, installmentAmounts);
            BigDecimal residualDuration = calculateResidualDuration(inputData, residualAmount, previousResidualDuration, installmentAmounts);
            return new Residual(residualAmount, residualDuration);
        }
    }

    private BigDecimal calculateResidualDuration(
        InputData inputData,
        BigDecimal residualAmount,
        BigDecimal previousResidualDuration,
        InstallmentAmounts installmentAmounts
    ) {
        if (installmentAmounts.overpayment().amount().compareTo(BigDecimal.ZERO) > 0) {
            return switch (inputData.installmentType()) {
                case CONSTANT -> calculateConstantResidualDuration(inputData, residualAmount, installmentAmounts);
                case DECREASING -> calculateDecreasingResidualDuration(residualAmount, installmentAmounts);
            };
        } else {
            return previousResidualDuration.subtract(BigDecimal.ONE);
        }
    }

    private BigDecimal calculateDecreasingResidualDuration(BigDecimal residualAmount, InstallmentAmounts installmentAmounts) {
        return residualAmount.divide(installmentAmounts.capitalAmount(), 0, RoundingMode.CEILING);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private BigDecimal calculateConstantResidualDuration(InputData inputData, BigDecimal residualAmount, InstallmentAmounts installmentAmounts) {
        // log_y(x) = log(x) / log (y)
        BigDecimal q = AmountsCalculationService.calculateQ(inputData.interestPercent());

        BigDecimal xNumerator = installmentAmounts.installmentAmount();
        BigDecimal xDenominator = installmentAmounts.installmentAmount().subtract(residualAmount.multiply(q.subtract(BigDecimal.ONE)));
        BigDecimal x = xNumerator.divide(xDenominator, 10, RoundingMode.HALF_UP);
        BigDecimal y = q;
        BigDecimal logX = BigDecimal.valueOf(Math.log(x.doubleValue()));
        BigDecimal logY = BigDecimal.valueOf(Math.log(y.doubleValue()));

        return logX.divide(logY, 0, RoundingMode.CEILING);
    }

    private BigDecimal calculateResidualAmount(final BigDecimal residualAmount, final InstallmentAmounts installmentAmounts) {
        return residualAmount
            .subtract(installmentAmounts.capitalAmount())
            .subtract(installmentAmounts.overpayment().amount())
            .max(BigDecimal.ZERO);
    }

}
