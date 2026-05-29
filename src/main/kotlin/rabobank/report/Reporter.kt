package rabobank.report

import org.slf4j.LoggerFactory
import rabobank.model.StatementError
import rabobank.model.StatementRecord
import java.math.BigDecimal

class Reporter {
    private val log = LoggerFactory.getLogger(Reporter::class.java)
    private val errors = mutableListOf<StatementError>()

    val statementErrors: List<StatementError> get() = errors.toList()

    fun reportFailure(record: StatementRecord, message: String) {
        errors.add(
            StatementError(
                sourceFile = record.sourceFile,
                sourceLine = record.sourceLine,
                errorMessage = message,
            )
        )
        log.warn(
            "Failure at {}:{} ref={} \"{}\" - {}",
            record.sourceFile,
            record.sourceLine,
            record.reference,
            record.description,
            message,
        )
    }

    fun reportBalances(balances: Map<String, BigDecimal>) {
        if (errors.isEmpty()) log.info("No failed records.")
        if (balances.isEmpty()) {
            log.info("Report complete: {} failure(s), no account activity.", errors.size)
            return
        }
        log.info("Report complete: {} failure(s), {} account(s).", errors.size, balances.size)
    }
}
