package pl.inbank.overpayment.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;


@With
public record InputData(
        LocalDate repaymentStartDate,
        BigDecimal wiborPercent,
        @Getter
        Map<Integer, BigDecimal> wiborChanges,
        BigDecimal amount,
        BigDecimal monthsDuration,
        OverpaymentType installmentType,
        BigDecimal marginPercent,
        BigDecimal overpaymentProvisionPercent,
        BigDecimal overpaymentProvisionMonths,
        BigDecimal overpaymentStartMonth,
        Map<Integer, BigDecimal> overpaymentSchema,
        String overpaymentReduceWay,
        boolean printPayoffsSchedule,
        Integer installmentNumberToPrint,
        BigDecimal loanGrantingFee,
        BigDecimal monthlyCommissionFee
) {

    @Builder
    public InputData {

    }

    public BigDecimal wiborPercentForInstallment(BigDecimal installmentNumber) {
        BigDecimal currentWibor = this.wiborPercent;
        BigDecimal lastChangeInstallmentNumber = BigDecimal.ZERO;

        for (Map.Entry<Integer, BigDecimal> entry : this.wiborChanges.entrySet()) {
            BigDecimal entryInstallmentNumber = BigDecimal.valueOf(entry.getKey());
            if (installmentNumber.compareTo(entryInstallmentNumber) >= 0 && entryInstallmentNumber.compareTo(lastChangeInstallmentNumber) > 0) {
                currentWibor = entry.getValue();
                lastChangeInstallmentNumber = entryInstallmentNumber;
            }
        }
        return currentWibor.divide(PERCENT, 4, RoundingMode.HALF_UP);
    }

    private static final BigDecimal PERCENT = new BigDecimal("100");

    public BigDecimal wiborPercent() {
        return wiborPercent.divide(PERCENT, 4, RoundingMode.HALF_UP);
    }

    public BigDecimal marginPercent() {
        return marginPercent.divide(PERCENT, 4, RoundingMode.HALF_UP);
    }


    public BigDecimal overpaymentProvisionPercent() {
        return overpaymentProvisionPercent.divide(PERCENT, 4, RoundingMode.HALF_UP);
    }

    public BigDecimal interestPercent() {
        return marginPercent().add(wiborPercent());
    }

    public String interestToDisplay() {
        BigDecimal interest = wiborPercent().add(marginPercent());
        interest = interest.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
        return interest.toString();
    }
}
