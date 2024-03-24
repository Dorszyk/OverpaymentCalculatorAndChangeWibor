package pl.inbank.overpayment.fixtures;

import pl.inbank.overpayment.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;


public class TestDataFixtures {

    public static Installment someInstallment() {
        return Installment.builder()
                .installmentNumber(BigDecimal.valueOf(10))
                .timePoint(someTimePoint())
                .installmentAmounts(someInstallmentAmounts())
                .residual(someResidual())
                .reference(someReference())
                .build();
    }

    public static Reference someReference() {
        return Reference.builder()
                .referenceAmount(new BigDecimal("235243.12"))
                .referenceDuration(BigDecimal.valueOf(83))
                .build();
    }

    public static Residual someResidual() {
        return Residual.builder()
                .residualAmount(new BigDecimal("662364.12"))
                .residualDuration(BigDecimal.valueOf(16))
                .build();
    }

    private static Map<Integer, BigDecimal> createWiborChanges() {
        return Map.of(
                (0), BigDecimal.valueOf(0.00)
        );
    }


    public static InputData someInputData() {
        return InputData.builder()
                .repaymentStartDate(LocalDate.of(2010, 5, 10))
                .wiborPercent(BigDecimal.valueOf(2.70))
                .wiborChanges(createWiborChanges())
                .amount(BigDecimal.valueOf(198267.46))
                .monthsDuration(BigDecimal.valueOf(180))
                .installmentType(OverpaymentType.CONSTANT)
                .marginPercent(BigDecimal.valueOf(1.8))
                .overpaymentProvisionPercent(BigDecimal.valueOf(3))
                .overpaymentProvisionMonths(BigDecimal.valueOf(36))
                .overpaymentStartMonth(BigDecimal.valueOf(1))
                .overpaymentSchema(Map.of())
                .overpaymentReduceWay(Overpayment.REDUCE_PERIOD)
                .printPayoffsSchedule(true)
                .installmentNumberToPrint(1)
                .loanGrantingFee(BigDecimal.valueOf(0.00))
                .monthlyCommissionFee(BigDecimal.valueOf(0.00))
                .build();
    }

    public static TimePoint someTimePoint() {
        return TimePoint.builder()
                .year(BigDecimal.valueOf(1))
                .month(BigDecimal.valueOf(1))
                .date(LocalDate.of(2010, 5, 10))
                .build();
    }

    public static Overpayment someOverpayment() {
        return Overpayment.builder()
                .amount(new BigDecimal("1255.02"))
                .provisionAmount(new BigDecimal("102"))
                .build();
    }

    public static InstallmentAmounts someInstallmentAmounts() {
        return InstallmentAmounts.builder()
                .installmentAmount(new BigDecimal("1516.73"))
                .capitalAmount(new BigDecimal("748.44"))
                .interestAmount(new BigDecimal("768.29"))
                .monthlyFee(new BigDecimal("0.0"))
                .build();
    }

    public static Installment someInstallment5() {
        return Installment.builder()
                .installmentNumber(BigDecimal.valueOf(5))
                .timePoint(TimePoint.builder()
                        .year(BigDecimal.ONE)
                        .month(BigDecimal.valueOf(5))
                        .date(LocalDate.of(2010, 9, 10))
                        .build())
                .installmentAmounts(InstallmentAmounts.builder()
                        .installmentAmount(new BigDecimal("1516.73"))
                        .interestAmount(new BigDecimal("732.12"))
                        .capitalAmount(new BigDecimal("784.61"))
                        .monthlyFee(new BigDecimal("0.0"))
                        .overpayment(Overpayment.builder()
                                .amount(BigDecimal.ZERO)
                                .provisionAmount(BigDecimal.ZERO)
                                .build())
                        .build())
                .residual(Residual.builder()
                        .residualAmount(new BigDecimal("194446.76"))
                        .residualDuration(new BigDecimal("175"))
                        .build())
                .reference(Reference.builder()
                        .referenceAmount(new BigDecimal("198267.46"))
                        .referenceDuration(new BigDecimal("180"))
                        .build())
                .build();
    }

    public static Installment someInstallment10() {
        return Installment.builder()
                .installmentNumber(BigDecimal.valueOf(10))
                .timePoint(TimePoint.builder()
                        .year(BigDecimal.ONE)
                        .month(BigDecimal.valueOf(10))
                        .date(LocalDate.of(2011, 2, 10))
                        .build())
                .installmentAmounts(InstallmentAmounts.builder()
                        .installmentAmount(new BigDecimal("1516.73"))
                        .interestAmount(new BigDecimal("669.73"))
                        .capitalAmount(new BigDecimal("847.00"))
                        .monthlyFee(new BigDecimal("0.0"))
                        .overpayment(Overpayment.builder()
                                .amount(BigDecimal.ZERO)
                                .provisionAmount(BigDecimal.ZERO)
                                .build())
                        .build())
                .residual(Residual.builder()
                        .residualAmount(new BigDecimal("190504.57"))
                        .residualDuration(new BigDecimal("170"))
                        .build())
                .reference(Reference.builder()
                        .referenceAmount(new BigDecimal("198267.46"))
                        .referenceDuration(new BigDecimal("180"))
                        .build())
                .build();
    }

    public static Installment someInstallment40() {
        return Installment.builder()
                .installmentNumber(BigDecimal.valueOf(40))
                .timePoint(TimePoint.builder()
                        .year(BigDecimal.valueOf(4))
                        .month(BigDecimal.valueOf(4))
                        .date(LocalDate.of(2013, 8, 10))
                        .build())
                .installmentAmounts(InstallmentAmounts.builder()
                        .installmentAmount(new BigDecimal("1516.73"))
                        .interestAmount(new BigDecimal("644.44"))
                        .capitalAmount(new BigDecimal("872.29"))
                        .monthlyFee(new BigDecimal("0.0"))
                        .overpayment(Overpayment.builder()
                                .amount(BigDecimal.ZERO)
                                .provisionAmount(BigDecimal.ZERO)
                                .build())
                        .build())
                .residual(Residual.builder()
                        .residualAmount(new BigDecimal("165434.81"))
                        .residualDuration(new BigDecimal("140"))
                        .build())
                .reference(Reference.builder()
                        .referenceAmount(new BigDecimal("198267.46"))
                        .referenceDuration(new BigDecimal("180"))
                        .build())
                .build();
    }

    public static Installment someInstallment80() {
        return Installment.builder()
                .installmentNumber(BigDecimal.valueOf(80))
                .timePoint(TimePoint.builder()
                        .year(BigDecimal.valueOf(7))
                        .month(BigDecimal.valueOf(8))
                        .date(LocalDate.of(2016, 12, 10))
                        .build())
                .installmentAmounts(InstallmentAmounts.builder()
                        .installmentAmount(new BigDecimal("1516.73"))
                        .interestAmount(new BigDecimal("496.78"))
                        .capitalAmount(new BigDecimal("1019.95"))
                        .monthlyFee(new BigDecimal("0.0"))
                        .overpayment(Overpayment.builder()
                                .amount(BigDecimal.ZERO)
                                .provisionAmount(BigDecimal.ZERO).build())
                        .build())
                .residual(Residual.builder()
                        .residualAmount(new BigDecimal("127181.22"))
                        .residualDuration(new BigDecimal("100"))
                        .build())
                .reference(Reference.builder()
                        .referenceAmount(new BigDecimal("198267.46"))
                        .referenceDuration(new BigDecimal("180"))
                        .build())
                .build();
    }
}
