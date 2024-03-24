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
public class DecreasingAmountsCalculationServiceImpl implements DecreasingAmountsCalculationService {

    @Override
    public InstallmentAmounts calculate(final InputData inputData, final Overpayment overpayment) {
        BigDecimal interestPercent = inputData.interestPercent(); // Konwersja stopy procentowej na ułamek
        log.info("InterestPercent: [{}]", interestPercent);

        final BigDecimal residualAmount = inputData.amount();
        final BigDecimal residualDuration = inputData.monthsDuration();


        LocalDate previousPaymentDate = inputData.repaymentStartDate(); // Zakładamy, że jest to data rozpoczęcia kredytu
        LocalDate currentPaymentDate = inputData.repaymentStartDate().plusMonths(1); // Data następnej spłaty jako punkt końcowy

        // Obliczenie kwoty odsetek z uwzględnieniem rzeczywistej liczby dni
        BigDecimal interestAmount = AmountsCalculationService.calculateInterestAmount(
                residualAmount, interestPercent, previousPaymentDate, currentPaymentDate);

        BigDecimal capitalAmount = AmountsCalculationService.compareCapitalWithResidual(
                calculateDecreasingCapitalAmount(residualAmount, residualDuration), residualAmount);

        BigDecimal commissionAmount = inputData.monthlyCommissionFee();
        BigDecimal installmentAmount = capitalAmount.add(interestAmount).add(commissionAmount);

        log.info(
                "Calculated installment: [{}], residualAmount: [{}], interestAmount: [{}], capitalAmount: [{}], installmentAmount: [{}], commissionAmount: [{}]",
                1, residualAmount, interestAmount, capitalAmount, installmentAmount, commissionAmount);

        return new InstallmentAmounts(installmentAmount, interestAmount, capitalAmount, overpayment, commissionAmount);
    }


    @Override
    public InstallmentAmounts calculate(final InputData inputData, final Overpayment overpayment, final Installment previousInstallment) {
        // Pobranie aktualnej wartości WIBOR dla bieżącej raty
        BigDecimal currentInstallmentNumber = previousInstallment.installmentNumber().add(BigDecimal.ONE);
        BigDecimal wiborPercent = inputData.wiborPercentForInstallment(currentInstallmentNumber);
        BigDecimal interestPercent = wiborPercent.add(inputData.marginPercent()); // Dodanie marży do WIBOR

        // Ustalenie daty dla obliczenia odsetek na podstawie daty rozpoczęcia spłaty i numeru raty
        LocalDate previousPaymentDate, currentPaymentDate;
        previousPaymentDate = inputData.repaymentStartDate().plusMonths(currentInstallmentNumber.intValue() - 1);
        currentPaymentDate = inputData.repaymentStartDate().plusMonths(currentInstallmentNumber.intValue());

        // Obliczenie kwoty odsetek z uwzględnieniem aktualnej wartości WIBOR oraz rzeczywistej liczby dni

        BigDecimal residualAmount = previousInstallment.residual().residualAmount();
        BigDecimal interestAmount = AmountsCalculationService.calculateInterestAmount(
                residualAmount, interestPercent, previousPaymentDate, currentPaymentDate);

        BigDecimal referenceAmount = previousInstallment.reference().referenceAmount();
        BigDecimal referenceDuration = previousInstallment.reference().referenceDuration();

        // Obliczenie kwoty kapitałowej
        BigDecimal capitalAmount = AmountsCalculationService.compareCapitalWithResidual(
                calculateDecreasingCapitalAmount(referenceAmount, referenceDuration), residualAmount);

        // Pobranie opłaty prowizyjnej
        BigDecimal commissionAmount = inputData.monthlyCommissionFee();
        // Obliczenie całkowitej kwoty raty
        BigDecimal installmentAmount = capitalAmount.add(interestAmount).add(commissionAmount);

        log.info(
                "Calculated installment: [{}], residualAmount: [{}], interestAmount: [{}], capitalAmount: [{}], installmentAmount: [{}], commissionAmount: [{}], " +
                        "referenceAmount: [{}], referenceDuration: [{}], WIBOR: [{}]",
                currentInstallmentNumber, residualAmount, interestAmount, capitalAmount, installmentAmount,
                commissionAmount, referenceAmount, referenceDuration, wiborPercent);

        return new InstallmentAmounts(installmentAmount, interestAmount, capitalAmount, overpayment, commissionAmount);
    }

    private BigDecimal calculateDecreasingCapitalAmount(final BigDecimal residualAmount, final BigDecimal residualDuration) {
        return residualAmount.divide(residualDuration, 2, RoundingMode.HALF_UP);
    }
}
