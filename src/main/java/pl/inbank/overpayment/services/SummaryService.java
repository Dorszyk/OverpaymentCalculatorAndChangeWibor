package pl.inbank.overpayment.services;

import pl.inbank.overpayment.model.Installment;
import pl.inbank.overpayment.model.Summary;

import java.util.List;

public interface SummaryService {

    Summary calculateSummary(List<Installment> installments);
}
