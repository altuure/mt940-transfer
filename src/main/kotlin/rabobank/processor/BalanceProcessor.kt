package rabobank.processor

import org.slf4j.LoggerFactory
import rabobank.model.StatementRecord

class BalanceProcessor : RecordProcessor {
    private val log = LoggerFactory.getLogger(BalanceProcessor::class.java)

    override fun process(record: StatementRecord, chain: ProcessorChain): ProcessResult {
        val expected = record.startBalance + record.mutation
        if (expected.compareTo(record.endBalance) != 0) {
            log.info(
                "Balance mismatch ref={} expected={} actual={}",
                record.reference, expected, record.endBalance,
            )
            return ProcessFailResult(
                "end balance ${record.endBalance} ≠ start ${record.startBalance} + mutation ${record.mutation} (expected $expected)",
            )
        }
        return chain.proceed(record)
    }
}
