package pl.inbank.overpayment.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.Installment;
import pl.inbank.overpayment.model.InstallmentAmounts;
import pl.inbank.overpayment.model.Overpayment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Slf4j
@Service
public class ConstantAmountsCalculationServiceImpl implements ConstantAmountsCalculationService {

    @Override
    public InstallmentAmounts calculate(final InputData inputData, final Overpayment overpayment) {
        LocalDate currentPaymentDate = inputData.repaymentStartDate().plusMonths(1);
        LocalDate previousPaymentDate = inputData.repaymentStartDate();

        BigDecimal currentInstallmentNumber = BigDecimal.ONE; // Zakładamy, że to pierwsza rata
        BigDecimal wiborPercent = inputData.wiborPercentForInstallment(currentInstallmentNumber);
        BigDecimal interestPercent = wiborPercent.add(inputData.marginPercent());

        BigDecimal residualAmount = inputData.amount();
        BigDecimal interestAmount = AmountsCalculationService.calculateInterestAmount(
                residualAmount, interestPercent, previousPaymentDate, currentPaymentDate);

        BigDecimal q = AmountsCalculationService.calculateQ(interestPercent);

        BigDecimal installmentAmountWithoutCommission;
        if (inputData.fixedMonthlyPayment() != null && wiborPercent.compareTo(inputData.wiborPercent()) == 0) {
            // Użyj stałej raty, jeśli WIBOR się nie zmienił
            installmentAmountWithoutCommission = inputData.fixedMonthlyPayment();
        } else {
            // Oblicz nową ratę z uwzględnieniem zmienionego WIBOR
            installmentAmountWithoutCommission = calculateConstantInstallmentAmount(
                    q, interestAmount, residualAmount, inputData.amount(), inputData.monthsDuration());
        }

        BigDecimal capitalAmount = installmentAmountWithoutCommission.subtract(interestAmount).setScale(2,RoundingMode.HALF_UP);
        BigDecimal commissionAmount = inputData.monthlyCommissionFee();
        BigDecimal installmentAmount = installmentAmountWithoutCommission.add(commissionAmount);

        log.info("Calculated installment: [{}], residualAmount: [{}], interestAmount: [{}], " +
                        "capitalAmount: [{}], installmentAmount: [{}], commissionAmount: [{}]",
                currentInstallmentNumber, residualAmount, interestAmount, capitalAmount, installmentAmount, commissionAmount);

        return new InstallmentAmounts(installmentAmount, interestAmount, capitalAmount, overpayment, commissionAmount);
    }

    @Override
    public InstallmentAmounts calculate(final InputData inputData, final Overpayment overpayment, final Installment previousInstallment) {
        // Pobieranie informacji o numerze bieżącej raty
        BigDecimal currentInstallmentNumber = previousInstallment.installmentNumber().add(BigDecimal.ONE);
        log.info("CurrentInstallmentNumber: [{}]", currentInstallmentNumber);

        // Pobranie aktualnej wartości WIBOR dla tej raty
        BigDecimal wiborPercent = inputData.wiborPercentForInstallment(currentInstallmentNumber);
        log.info("WiborPercent: [{}]", wiborPercent);

        BigDecimal interestPercent = wiborPercent.add(inputData.marginPercent()); // Dodanie marży do WIBOR
        log.info("interestPercent: [{}]", interestPercent);

        // Obliczanie wartości współczynnika q dla annuitetu
        BigDecimal q = AmountsCalculationService.calculateQ(interestPercent);
        log.info("Q: [{}]", q);

        // Pobranie kwoty residualnej z ostatniej raty
        BigDecimal residualAmount = previousInstallment.residual().residualAmount();
        log.info("residualAmount: [{}]", residualAmount);

        // Ustawianie dat dla obliczeń odsetek
        LocalDate previousPaymentDate = inputData.repaymentStartDate().plusMonths(currentInstallmentNumber.intValue() - 1);
        LocalDate currentPaymentDate = inputData.repaymentStartDate().plusMonths(currentInstallmentNumber.intValue());
        log.info("PreviousPaymentDate: [{}], CurrentPaymentDate: [{}]", previousPaymentDate, currentPaymentDate);

        // Obliczenie odsetek dla danego okresu
        BigDecimal interestAmount = AmountsCalculationService.calculateInterestAmount(
                residualAmount, interestPercent, previousPaymentDate, currentPaymentDate);
        log.info("interestAmount: [{}]", interestAmount);

        // Obliczenie kwoty raty bez uwzględnienia prowizji
        BigDecimal installmentAmountWithoutCommission;
        if (inputData.fixedMonthlyPayment() != null && wiborPercent.equals(inputData.wiborPercent())) {
            // Użyj stałej raty, jeśli WIBOR się nie zmienił
            installmentAmountWithoutCommission = inputData.fixedMonthlyPayment();
        } else {
            // Przelicz ratę na nowo, jeśli WIBOR się zmienił
            installmentAmountWithoutCommission = calculateConstantInstallmentAmount(
                    q, interestAmount, residualAmount, inputData.amount(), inputData.monthsDuration());
        }
        log.info("InstallmentAmountWithoutCommission: [{}]", installmentAmountWithoutCommission);

        // Obliczenie kwoty kapitałowej
        BigDecimal capitalAmount = installmentAmountWithoutCommission.subtract(interestAmount).setScale(2,RoundingMode.HALF_DOWN);
        log.info("CapitalAmount: [{}]", capitalAmount);

        // Pobranie opłaty prowizyjnej
        BigDecimal commissionAmount = inputData.monthlyCommissionFee();
        log.info("CommissionAmount: [{}]", commissionAmount);

        // Dodanie opłaty prowizyjnej do całkowitej kwoty raty
        BigDecimal installmentAmount = installmentAmountWithoutCommission.add(commissionAmount);
        log.info("InstallmentAmount: [{}]", installmentAmount);

        log.info(
                "Calculated installment: [{}], residualAmount: [{}], interestAmount: [{}], capitalAmount: [{}], installmentAmount: [{}], commissionAmount: [{}]",
                currentInstallmentNumber, residualAmount, interestAmount, capitalAmount, installmentAmount, commissionAmount);

        return new InstallmentAmounts(installmentAmount, interestAmount, capitalAmount, overpayment, commissionAmount);
    }


    private BigDecimal calculateConstantInstallmentAmount(
            final BigDecimal q,
            final BigDecimal interestAmount,
            final BigDecimal residualAmount,
            final BigDecimal referenceAmount,
            final BigDecimal referenceDuration
    ) {
        BigDecimal installmentAmount = referenceAmount
                .multiply(q.pow(referenceDuration.intValue()))
                .multiply(q.subtract(BigDecimal.ONE))
                .divide(q.pow(referenceDuration.intValue()).subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);

        return compareInstallmentWithResidual(installmentAmount, interestAmount, residualAmount);
    }

    private BigDecimal compareInstallmentWithResidual(
            final BigDecimal installmentAmount,
            final BigDecimal interestAmount,
            final BigDecimal residualAmount
    ) {
        if (installmentAmount.subtract(interestAmount).compareTo(residualAmount) >= 0) {
            return residualAmount.add(interestAmount);
        }
        return installmentAmount;
    }

}
