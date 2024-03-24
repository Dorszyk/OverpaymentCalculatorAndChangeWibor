package pl.inbank.overpayment.services;

import pl.inbank.overpayment.model.InputData;
import pl.inbank.overpayment.model.Installment;
import pl.inbank.overpayment.model.Summary;

import java.util.List;

public interface PrintingService {

    String SCHEDULE_TABLE_FORMAT =
            "%-1s:%3s  " +
                    "%-1s:%3s  " +
                    "%-1s:%3s  " +
                    "%-7s:%5s  " +
                    "%-7s:%7s  " +
                    "%-7s:%7s  " +
                    "%-7s:%7s  " +
                    "%-7s:%3s  " +
                    "%-8s:%8s  " +
                    "%-8s:%9s  " +
                    "%-1s:%3s%n";

    List<String> INSTALLMENT_LINE_KEYS = List.of(
            "No",
            "Yr",
            "Month",
            "Payment date",
            "Monthly payment",
            "Interest amount",
            "Main part",
            "Administration fee",
            "Overpayment",
            "Balance",
            "No"
    );

    String INTRO_INFORMATION = """  
            Initial loan amount: %s PLN
            Period in months: %s
            Annual interest rate when granting a loan: %s%%
            Month of overpayments: %s months
            Contract fee: %s PLN
            Administration fee: %S PLN
            """;

    String OVERPAYMENT_INFORMATION = """
            
            %s
            Diagram of overpayment:
            %s""";

    String WIBOR_CHANGES_INFORMATION = """
            
            Diagram of WIBOR changes:
            %s
            """;

    String SUMMARY_INFORMATION = """
                    
            Sum of credit interest: %s PLN
            Commission for overpayment: %s PLN
            Total sum interest and commission overpayment: %s PL
            Total sum of capital repaid by the customer: %s PLN
            Total sum capital and interest: %s PLN
            Total administration Fee: %s PLN
            Total amount to be paid by the customer: %s PLN
                    
            """;

    String OVERPAYMENT_REDUCE_INSTALLMENT = "Overpayment - Reduce instalment ";
    String OVERPAYMENT_REDUCE_PERIOD = "Overpayment - Reduce period";
    String OVERPAYMENT_SCHEMA = "Monthly: %s, Sum: %s PLN%n";

    void printIntroInformation(InputData inputData);

    void printSchedule(List<Installment> installments, final InputData inputData);

    void printSummary(Summary summaryNoOverpayment);

}
