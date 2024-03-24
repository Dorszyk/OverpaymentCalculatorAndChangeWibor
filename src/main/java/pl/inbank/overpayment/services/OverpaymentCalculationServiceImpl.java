package pl.inbank.overpayment.services;

import org.springframework.stereotype.Service;
import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.Overpayment;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
public class OverpaymentCalculationServiceImpl implements OverpaymentCalculationService {

    @Override
    public Overpayment calculate(final BigDecimal installmentNumber, final InputData inputData) {
        BigDecimal overpaymentAmount = calculateOverpaymentAmount(installmentNumber, inputData.overpaymentSchema()).orElse(BigDecimal.ZERO);
        BigDecimal overpaymentProvision = calculateOverpaymentProvision(installmentNumber, overpaymentAmount, inputData);
        return new Overpayment(overpaymentAmount, overpaymentProvision);
    }

    private Optional<BigDecimal> calculateOverpaymentAmount(final BigDecimal installmentNumber, Map<Integer, BigDecimal> overpaymentSchema) {
        return overpaymentSchema.entrySet().stream()
            .filter(entry -> BigDecimal.valueOf(entry.getKey()).equals(installmentNumber))
            .findFirst()
            .map(Map.Entry::getValue);
    }

    private BigDecimal calculateOverpaymentProvision(final BigDecimal installmentNumber, final BigDecimal overpaymentAmount, final InputData inputData) {
        if (BigDecimal.ZERO.equals(overpaymentAmount)) {
            return BigDecimal.ZERO;
        }

        if (installmentNumber.compareTo(inputData.overpaymentProvisionMonths()) > 0) {
            return BigDecimal.ZERO;
        }

        return overpaymentAmount.multiply(inputData.overpaymentProvisionPercent());
    }

}
