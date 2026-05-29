package rabobank.processor

import org.slf4j.LoggerFactory
import rabobank.model.StatementRecord
import java.math.BigDecimal

class AccountBalanceCollector : RecordProcessor {
    private val log = LoggerFactory.getLogger(AccountBalanceCollector::class.java)
    private val balances = mutableMapOf<String, BigDecimal>()

    val accountBalances: Map<String, BigDecimal> get() = balances.toMap()

    override fun process(record: StatementRecord, chain: ProcessorChain): ProcessResult {
        val updated = balances.merge(record.accountNumber, record.mutation, BigDecimal::add)
        log.debug("Account {} net mutation now {}", record.accountNumber, updated)
        return chain.proceed(record)
    }
}
