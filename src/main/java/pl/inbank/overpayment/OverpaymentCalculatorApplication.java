package pl.inbank.overpayment;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import pl.inbank.overpayment.configuration.CalculatorConfiguration;
import pl.inbank.overpayment.services.CalculationService;

public class OverpaymentCalculatorApplication {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(CalculatorConfiguration.class);
        CalculationService calculationService = context.getBean(CalculationService.class);
        calculationService.calculate();
    }
}
