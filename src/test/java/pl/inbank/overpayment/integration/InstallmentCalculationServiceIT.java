package pl.inbank.overpayment.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import pl.inbank.overpayment.configuration.CalculatorConfiguration;
import pl.inbank.overpayment.services.InstallmentCalculationService;

import static pl.inbank.overpayment.fixtures.TestDataFixtures.*;


@SpringJUnitConfig(classes = {CalculatorConfiguration.class})
public class InstallmentCalculationServiceIT {

    @Autowired
    @SuppressWarnings("unused")
    private InstallmentCalculationService installmentCalculationService;

    @BeforeEach
    public void setUp() {
        Assertions.assertNotNull(installmentCalculationService);
    }

    @Test
    @DisplayName("Test installment calculation")
    void test() {
        // given
        final var inputData = someInputData();

        // when
        final var result = installmentCalculationService.calculate(inputData);

        // then
        Assertions.assertEquals(180, result.size());
        Assertions.assertEquals(someInstallment5(), result.get(4));
        Assertions.assertEquals(someInstallment10(), result.get(9));
        Assertions.assertEquals(someInstallment40(), result.get(39));
        Assertions.assertEquals(someInstallment80(), result.get(79));
    }
}
