package pl.inbank.overpayment.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.Installment;
import pl.inbank.overpayment.model.Overpayment;
import pl.inbank.overpayment.model.Summary;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class PrintingServiceImpl implements PrintingService {


    private static final String SEPARATOR = createSeparator('-', 180);

    private static final Path RESULT_FILE_PATH = Paths.get("src/main/resources/calculationResult.csv");

    @SuppressWarnings("SameParameterValue")
    private static String createSeparator(char sign, int length) {
        return String.valueOf(sign).repeat(Math.max(0, length)) + System.lineSeparator();
    }

    @Override
    public void printIntroInformation(InputData inputData) {
        String introInformation = INTRO_INFORMATION
                .formatted(
                        inputData.amount(),
                        inputData.monthsDuration(),
                        inputData.interestToDisplay(),
                        inputData.overpaymentStartMonth(),
                        inputData.loanGrantingFee(),
                        inputData.monthlyCommissionFee()
                );

        if (Optional.ofNullable(inputData.overpaymentSchema()).map(schema -> schema.size() > 0).orElse(false)) {
            String overpaymentMessage = OVERPAYMENT_INFORMATION.formatted(
                    Overpayment.REDUCE_PERIOD.equals(inputData.overpaymentReduceWay())
                            ? OVERPAYMENT_REDUCE_PERIOD
                            : OVERPAYMENT_REDUCE_INSTALLMENT,
                    overpaymentSchemaMessage(inputData.overpaymentSchema())
            );
            introInformation += overpaymentMessage;
        }
        String wiborChangesMessage = wiborChangesMessage(inputData.getWiborChanges());
        introInformation += WIBOR_CHANGES_INFORMATION.formatted(wiborChangesMessage);

        log(introInformation);
    }


    private String wiborChangesMessage(Map<Integer, BigDecimal> wiborChanges) {
        if (wiborChanges == null || wiborChanges.isEmpty()) {
            return "No planned WIBOR changes.";
        }
        StringBuilder message = new StringBuilder();
        wiborChanges.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> message.append(String.format(Locale.US,
                        "Monthly: %d, change WIBOR: %.2f%%\n",
                        entry.getKey(),
                        entry.getValue().setScale(2, RoundingMode.HALF_UP)))
                );
        return message.toString().trim();
    }

    private String overpaymentSchemaMessage(Map<Integer, BigDecimal> schema) {
        return schema.entrySet().stream()
                .reduce(
                        new StringBuilder(),
                        (previous, next) -> previous.append(String.format(OVERPAYMENT_SCHEMA, next.getKey(), next.getValue())),
                        StringBuilder::append)
                .toString();
    }

    @Override
    public void printSchedule(final List<Installment> installments, final InputData inputData) {
        if (!inputData.printPayoffsSchedule()) {
            return;
        }

        installments.stream()
                .filter(installment -> installment.installmentNumber()
                        .remainder(BigDecimal.valueOf(inputData.installmentNumberToPrint())).equals(BigDecimal.ZERO))
                .forEach(installment -> {
                    log(formatInstallmentLine(installment));
                   if (AmountsCalculationService.YEAR.equals(installment.timePoint().month())) {
                        log(SEPARATOR);
                    }
                });

        log(System.lineSeparator());
    }

    private String formatInstallmentLine(Installment installment) {
        return String.format(SCHEDULE_TABLE_FORMAT,
                INSTALLMENT_LINE_KEYS.get(0), installment.installmentNumber(),
                INSTALLMENT_LINE_KEYS.get(1), installment.timePoint().year(),
                INSTALLMENT_LINE_KEYS.get(2), installment.timePoint().month(),
                INSTALLMENT_LINE_KEYS.get(3), installment.timePoint().date(),
                INSTALLMENT_LINE_KEYS.get(4), installment.installmentAmounts().installmentAmount(),
                INSTALLMENT_LINE_KEYS.get(5), installment.installmentAmounts().interestAmount(),
                INSTALLMENT_LINE_KEYS.get(6), installment.installmentAmounts().capitalAmount(),
                INSTALLMENT_LINE_KEYS.get(7), installment.installmentAmounts().monthlyFee(),
                INSTALLMENT_LINE_KEYS.get(8), installment.installmentAmounts().overpayment().amount(),
                INSTALLMENT_LINE_KEYS.get(9), installment.residual().residualAmount(),
                INSTALLMENT_LINE_KEYS.get(10), installment.residual().residualDuration()
        );
    }

    @Override
    public void printSummary(final Summary summary) {
        log(SUMMARY_INFORMATION.formatted(
                summary.interestSum(),
                summary.overpaymentProvisionSum().setScale(2, RoundingMode.HALF_UP),
                summary.totalLostSum().setScale(2, RoundingMode.HALF_UP),
                summary.totalCapital().setScale(2, RoundingMode.HALF_UP),
                summary.totalInterestAndCapital().setScale(2, RoundingMode.HALF_UP),
                summary.totalAdministrationFee().setScale(2, RoundingMode.HALF_UP),
                summary.totalAmountPaid().setScale(2,RoundingMode.HALF_UP)
        ));
    }

    private void log(String message) {
        try {
            if (!Files.exists(RESULT_FILE_PATH)) {
                Files.createFile(RESULT_FILE_PATH);
            }
            Files.writeString(RESULT_FILE_PATH, message, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Error writing data to file", e);
        }
    }

}
