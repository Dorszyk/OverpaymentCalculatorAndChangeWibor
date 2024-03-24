package pl.inbank.overpayment.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.inbank.overpayment.fixtures.TestDataFixtures;
import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.Installment;
import pl.inbank.overpayment.model.InstallmentAmounts;
import pl.inbank.overpayment.model.Overpayment;


import java.math.BigDecimal;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstallmentCalculationServiceImplTest {

    @InjectMocks
    private InstallmentCalculationServiceImpl installmentCalculationService;

    @Mock
    private TimePointCalculationService timePointCalculationService;

    @Mock
    private AmountsCalculationService amountsCalculationService;

    @Mock
    private ResidualCalculationService residualCalculationService;

    @Mock
    private ReferenceCalculationService referenceCalculationService;

    @Mock
    private OverpaymentCalculationService overpaymentCalculationService;

    @Test
    void thatInstallmentsAreCalculatedCorrectly() {
        // given
        final var inputData = TestDataFixtures.someInputData();
        final var expected = IntStream.rangeClosed(1, 180).boxed()
            .map(this::createInstallment)
            .toList();

        when(timePointCalculationService.calculate(any(BigDecimal.class), any(InputData.class)))
            .thenReturn(TestDataFixtures.someTimePoint());
        when(timePointCalculationService.calculate(any(BigDecimal.class), any(Installment.class)))
            .thenReturn(TestDataFixtures.someTimePoint());

        when(amountsCalculationService.calculate(any(InputData.class), any(Overpayment.class)))
            .thenReturn(TestDataFixtures.someInstallmentAmounts());
        when(amountsCalculationService.calculate(any(InputData.class), any(Overpayment.class), any(BigDecimal.class), any(Installment.class)))
                .thenReturn(TestDataFixtures.someInstallmentAmounts());

        when(residualCalculationService.calculate(any(InstallmentAmounts.class), any(InputData.class)))
            .thenReturn(TestDataFixtures.someResidual());
        when(residualCalculationService.calculate(any(InstallmentAmounts.class), any(InputData.class), any(Installment.class)))
            .thenReturn(TestDataFixtures.someResidual());

        when(referenceCalculationService.calculate(any(InstallmentAmounts.class), any(InputData.class)))
            .thenReturn(TestDataFixtures.someReference());
        when(referenceCalculationService.calculate(any(InstallmentAmounts.class), any(InputData.class), any(Installment.class)))
            .thenReturn(TestDataFixtures.someReference());

        when(overpaymentCalculationService.calculate(any(BigDecimal.class), any(InputData.class)))
            .thenReturn(TestDataFixtures.someOverpayment());

        // when
        final var result = installmentCalculationService.calculate(inputData);

        // then
        Assertions.assertEquals(expected, result);
    }

    private Installment createInstallment(final Integer index) {
        return TestDataFixtures.someInstallment()
            .withInstallmentNumber(BigDecimal.valueOf(index));
    }

}