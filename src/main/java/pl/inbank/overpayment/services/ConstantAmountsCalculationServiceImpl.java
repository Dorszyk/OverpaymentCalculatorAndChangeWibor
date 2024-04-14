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
        BigDecimal interestPercent = inputData.interestPercent();
        log.info("InterestPercent: [{}]", interestPercent);

        BigDecimal residualAmount = inputData.amount();
        log.info("ResidualAmount: [{}]", residualAmount);

        // Ustalenie dat startowych i końcowych dla obliczenia odsetek
        LocalDate previousPaymentDate = inputData.repaymentStartDate();// Zakładamy, że jest to data rozpoczęcia kredytu
        log.info("PreviousPaymentDate: [{}]",previousPaymentDate);

        LocalDate currentPaymentDate = inputData.repaymentStartDate().plusMonths(1); // Data następnej spłaty jako punkt końcowy
        log.info("CurrentPaymentDate: [{}]",currentPaymentDate);

        // Obliczenie kwoty odsetek z uwzględnieniem rzeczywistych dni
        BigDecimal interestAmount = AmountsCalculationService.calculateInterestAmount(
                residualAmount, interestPercent, previousPaymentDate, currentPaymentDate);
        log.info("InterestAmount: [{}]",interestAmount);

        BigDecimal q = AmountsCalculationService.calculateQ(interestPercent);
        log.info("Q: [{}]", q);

        // Obliczenie stałej kwoty raty bez uwzględnienia prowizji
        BigDecimal installmentAmountWithoutCommission = calculateConstantInstallmentAmount(
                q, interestAmount, residualAmount, inputData.amount(), inputData.monthsDuration());
        log.info("InstallmentAmountWithoutCommission: [{}]",installmentAmountWithoutCommission);

        // Obliczenie kwoty kapitałowej jako różnicy między całkowitą kwotą raty (bez prowizji) a kwotą odsetek
        BigDecimal capitalAmount = AmountsCalculationService.compareCapitalWithResidual(
                installmentAmountWithoutCommission.subtract(interestAmount), residualAmount);
        log.info("CapitalAmount: [{}]",capitalAmount);

        // Pobranie opłaty prowizyjnej
        BigDecimal commissionAmount = inputData.monthlyCommissionFee();
        log.info("CommissionAmount: [{}]",commissionAmount);

        // Dodanie opłaty prowizyjnej do całkowitej kwoty raty
        BigDecimal installmentAmount = installmentAmountWithoutCommission.add(commissionAmount);
        log.info("InstallmentAmount: [{}]",installmentAmount);

        // Logowanie obliczeń
        log.info(
                "Calculated installment: [{}], residualAmount: [{}], interestAmount: [{}], capitalAmount: [{}], installmentAmount: [{}], commissionAmount: [{}]",
                1, residualAmount, interestAmount, capitalAmount, installmentAmount, commissionAmount);

        // Zwrócenie obliczonych wartości
        return new InstallmentAmounts(installmentAmount, interestAmount, capitalAmount, overpayment, commissionAmount);
    }

    @Override
    public InstallmentAmounts calculate(final InputData inputData, final Overpayment overpayment, final Installment previousInstallment) {
        // Pobranie aktualnej wartości WIBOR dla bieżącej raty
        BigDecimal currentInstallmentNumber = previousInstallment.installmentNumber().add(BigDecimal.ONE);
        log.info("CurrentInstallmentNumber: [{}]", currentInstallmentNumber);

        BigDecimal wiborPercent = inputData.wiborPercentForInstallment(previousInstallment.installmentNumber().add(BigDecimal.ONE));
        log.info("WiborPercent: [{}]", wiborPercent);

        BigDecimal interestPercent = wiborPercent.add(inputData.marginPercent()); // Dodanie marży do WIBOR
        log.info("interestPercent: [{}]", interestPercent);

        BigDecimal q = AmountsCalculationService.calculateQ(interestPercent);
        log.info("q: [{}]", q);


        BigDecimal residualAmount = previousInstallment.residual().residualAmount();
        log.info("residualAmount: [{}]", residualAmount);

        LocalDate previousPaymentDate, currentPaymentDate;
        previousPaymentDate = inputData.repaymentStartDate().plusMonths(currentInstallmentNumber.intValue() - 1);
        log.info("previousPaymentDate: [{}]", previousPaymentDate);

        currentPaymentDate = inputData.repaymentStartDate().plusMonths(currentInstallmentNumber.intValue());
        log.info("residualAmount: [{}]", residualAmount);

        // Obliczenie kwoty odsetek z uwzględnieniem aktualnej wartości WIBOR oraz rzeczywistej liczby dni
        BigDecimal interestAmount = AmountsCalculationService.calculateInterestAmount(
                residualAmount, interestPercent, previousPaymentDate, currentPaymentDate);
        log.info("interestAmount: [{}]", interestAmount);

        BigDecimal referenceAmount = previousInstallment.reference().referenceAmount();
        log.info("referenceAmount: [{}]", referenceAmount);

        BigDecimal referenceDuration = previousInstallment.reference().referenceDuration();
        log.info("referenceDuration: [{}]", referenceDuration);

        // Obliczenie stałej kwoty raty bez uwzględnienia prowizji
        BigDecimal installmentAmountWithoutCommission = calculateConstantInstallmentAmount(
                q, interestAmount, residualAmount, referenceAmount, referenceDuration);
        log.info("installmentAmountWithoutCommission: [{}]", installmentAmountWithoutCommission);

        // Obliczenie kwoty kapitałowej jako różnicy między całkowitą kwotą raty (bez prowizji) a kwotą odsetek
        BigDecimal capitalAmount = AmountsCalculationService.compareCapitalWithResidual(installmentAmountWithoutCommission.subtract(interestAmount), residualAmount);
        log.info("capitalAmount: [{}]", capitalAmount);
        // Pobranie opłaty prowizyjnej
        BigDecimal commissionAmount = inputData.monthlyCommissionFee();
        log.info("commissionAmount: [{}]", commissionAmount);


        // Dodanie opłaty prowizyjnej do całkowitej kwoty raty
        BigDecimal installmentAmount = installmentAmountWithoutCommission.add(commissionAmount);
        log.info("installmentAmount: [{}]", installmentAmount);

        log.info(
                "Calculated installment: [{}], residualAmount: [{}], interestAmount: [{}], capitalAmount: [{}], installmentAmount: [{}], " +
                        "commissionAmount: [{}], referenceAmount: [{}], referenceDuration: [{}], WIBOR: [{}]",
                previousInstallment.installmentNumber().add(BigDecimal.ONE), residualAmount, interestAmount, capitalAmount, installmentAmount,
                commissionAmount, referenceAmount, referenceDuration, wiborPercent);

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
