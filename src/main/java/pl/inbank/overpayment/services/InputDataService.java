package pl.inbank.overpayment.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.OverpaymentType;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class InputDataService {

    private static final String INPUT_DATA_CSV = "classpath:inputData.csv";

    public Optional<InputData> read() {
        final var content = readFile();
        if (content.isEmpty()) {
            return Optional.empty();
        }

        validate(content);

        var inputData = content.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0).split(";")[1]));

        return Optional.of(
                InputData.builder()
                        .repaymentStartDate(
                                Optional.ofNullable(inputData.get("repaymentStartDate")).map(LocalDate::parse).orElseThrow())
                        .fixedMonthlyPayment(
                                Optional.ofNullable(inputData.get("fixedMonthlyPayment")).map(BigDecimal::new).orElseThrow())
                        .wiborPercent(
                                Optional.ofNullable(inputData.get("wibor")).map(BigDecimal::new).orElseThrow())
                        .amount(
                                Optional.ofNullable(inputData.get("amount")).map(BigDecimal::new).orElseThrow())
                        .wiborChanges(
                                Optional.ofNullable(inputData.get("wiborChanges")).map(InputDataService::parseWiborChanges).orElseThrow())
                        .monthsDuration(
                                Optional.ofNullable(inputData.get("monthsDuration")).map(BigDecimal::new).orElseThrow())
                        .installmentType(
                                Optional.ofNullable(inputData.get("installmentType")).map(OverpaymentType::valueOf).orElseThrow())
                        .marginPercent(
                                Optional.ofNullable(inputData.get("margin")).map(BigDecimal::new).orElseThrow())
                        .overpaymentProvisionPercent(
                                Optional.ofNullable(inputData.get("overpaymentProvision")).map(BigDecimal::new).orElseThrow())
                        .overpaymentProvisionMonths(
                                Optional.ofNullable(inputData.get("overpaymentProvisionMonths")).map(BigDecimal::new).orElseThrow())
                        .overpaymentStartMonth(
                                Optional.ofNullable(inputData.get("overpaymentStartMonth")).map(BigDecimal::new).orElseThrow())
                        .overpaymentSchema(
                                Optional.ofNullable(inputData.get("overpaymentSchema")).map(InputDataService::calculateSchema).orElseThrow())
                        .overpaymentReduceWay(
                                Optional.ofNullable(inputData.get("overpaymentReduceWay")).orElseThrow())
                        .printPayoffsSchedule(
                                Optional.ofNullable(inputData.get("printPayoffsSchedule")).map(Boolean::parseBoolean).orElseThrow())
                        .installmentNumberToPrint(
                                Optional.ofNullable(inputData.get("installmentNumberToPrint")).map(Integer::parseInt).orElseThrow())
                        .loanGrantingFee(
                                Optional.ofNullable(inputData.get("loanGrantingFee")).map(BigDecimal::new).orElseThrow())
                        .monthlyCommissionFee(
                                Optional.ofNullable(inputData.get("monthlyCommissionFee")).map(BigDecimal::new).orElseThrow())
                        .build()
        );
    }

    private Map<String, List<String>> readFile() {
        try {
            File file = ResourceUtils.getFile(INPUT_DATA_CSV);
            return Files.readString(file.toPath())
                    .lines()
                    .collect(Collectors.groupingBy(line -> line.split(";")[0]));
        } catch (Exception | IOException e) {
            log.error("Error loading input data, interrupting", e);
            return Map.of();
        }
    }

    private static Map<Integer, BigDecimal> calculateSchema(String schema) {
        return Stream.of(schema.split(","))
                .map(entry -> Map.entry(
                        entry.split(":")[0],
                        entry.split(":")[1]))
                .collect(Collectors.toMap(
                        entry -> Integer.parseInt(entry.getKey()),
                        entry -> new BigDecimal(entry.getValue()),
                        (v1, v2) -> v2,
                        TreeMap::new
                ));
    }

    public static Map<Integer, BigDecimal> parseWiborChanges(String wiborChangesInput) {
        return Stream.of(wiborChangesInput.split(","))
                .map(entry -> {
                    String[] parts = entry.split(":");
                    return Map.entry(Integer.parseInt(parts[0]), new BigDecimal(parts[1]));
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existingValue, newValue) -> newValue,
                        TreeMap::new
                ));
    }

    private void validate(final Map<String, List<String>> content) {
        if (content.values().stream().anyMatch(values -> values.size() != 1)) {
            throw new IllegalArgumentException("Configuration mismatch");
        }
    }
}
