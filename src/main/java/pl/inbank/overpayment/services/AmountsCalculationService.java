package pl.inbank.overpayment.services;

import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.Installment;
import pl.inbank.overpayment.model.InstallmentAmounts;
import pl.inbank.overpayment.model.Overpayment;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public interface AmountsCalculationService {

    BigDecimal YEAR = BigDecimal.valueOf(12);

    InstallmentAmounts calculate(final InputData inputData, final Overpayment overpayment);

    InstallmentAmounts calculate(final InputData inputData, final Overpayment overpayment, final BigDecimal monthlyCommission, final Installment previousInstallment);

    public static BigDecimal calculateInterestAmount(
            final BigDecimal principalAmount,
            final BigDecimal annualInterestRate,
            final LocalDate startPaymentDate, // Data rozpoczęcia okresu dla obliczeń odsetek
            final LocalDate nextPaymentDate) { // Data następnej płatności

        // Oblicz liczby dni między datami
        long daysBetween = ChronoUnit.DAYS.between(startPaymentDate, nextPaymentDate);

        // Ustawienie liczby dni w roku na stałą wartość 360 dla 'bank year'
        BigDecimal daysInBankYear = BigDecimal.valueOf(360);

        // Oblicz dzienną stopę procentową
        BigDecimal dailyInterestRate = annualInterestRate.divide(daysInBankYear, MathContext.DECIMAL128);

        // Oblicz kwotę odsetek za okres między płatnościami
        BigDecimal interestAmountForPeriod = principalAmount
                .multiply(dailyInterestRate)
                .multiply(BigDecimal.valueOf(daysBetween));

        return interestAmountForPeriod.setScale(2, RoundingMode.HALF_EVEN);
    }




    static BigDecimal calculateQ(final BigDecimal interestPercent) {
        return interestPercent.divide(AmountsCalculationService.YEAR, 10, RoundingMode.HALF_UP).add(BigDecimal.ONE);
    }

    static BigDecimal compareCapitalWithResidual(final BigDecimal capitalAmount, final BigDecimal residualAmount) {
        if (capitalAmount.compareTo(residualAmount) >= 0) {
            return residualAmount;
        }
        return capitalAmount;
    }
}
