package pl.inbank.overpayment.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import pl.inbank.overpayment.OverpaymentCalculatorApplication;
import pl.inbank.overpayment.model.Installment;
import pl.inbank.overpayment.model.InstallmentAmounts;
import pl.inbank.overpayment.model.Summary;
import pl.inbank.overpayment.services.SummaryService;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

@Configuration
@ComponentScan(basePackageClasses = OverpaymentCalculatorApplication.class)
public class CalculatorConfiguration {

    @Bean
    public static SummaryService summaryService() {
        return installments -> {
            BigDecimal interestSum = calculate(installments, installment -> installment.installmentAmounts().interestAmount());
            BigDecimal overpaymentProvisionSum = calculate(installments, installment -> installment.installmentAmounts().overpayment().provisionAmount());
            BigDecimal totalCapital = calculate(installments, installment -> totalCapital(installment.installmentAmounts()));
            BigDecimal totalLostSum = interestSum.add(overpaymentProvisionSum);
            BigDecimal totalInterestAndCapital = installments.stream()
                    .map(installment -> installment.installmentAmounts().interestAmount()
                            .add(installment.installmentAmounts().capitalAmount())
                            .add(installment.installmentAmounts().overpayment().amount())
                    )
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalAdministrationFee = calculate(installments, installment -> installment.installmentAmounts().monthlyFee());
            BigDecimal totalAmountPaid = totalInterestAndCapital.add(overpaymentProvisionSum).add(totalAdministrationFee);
            return new Summary(interestSum, overpaymentProvisionSum, totalLostSum, totalCapital, totalInterestAndCapital, totalAdministrationFee,totalAmountPaid);
        };
    }

    private static BigDecimal totalCapital(final InstallmentAmounts installmentAmounts) {
        return installmentAmounts.capitalAmount().add(installmentAmounts.overpayment().amount());
    }

    private static BigDecimal calculate(final List<Installment> installments, Function<Installment, BigDecimal> function) {
        return installments.stream()
                .reduce(BigDecimal.ZERO, (sum, next) -> sum.add(function.apply(next)), BigDecimal::add);
    }
}
