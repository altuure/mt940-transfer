package rabobank.processor

import org.slf4j.LoggerFactory
import rabobank.model.StatementRecord

class UniqueReferenceProcessor : RecordProcessor {
    private val log = LoggerFactory.getLogger(UniqueReferenceProcessor::class.java)
    private val seen = mutableSetOf<Long>()

    override fun process(record: StatementRecord, chain: ProcessorChain): ProcessResult {
        if (seen.contains(record.reference)) { // reject if it is already found
            log.info("Duplicate reference detected: {}", record.reference)
            return ProcessFailResult("duplicate transaction reference ${record.reference}")
        }
        val proceed = chain.proceed(record)
        if (proceed is ProcessSuccessResult) { // add to registry only after full success
            seen.add(record.reference)
        }
        return proceed
    }
}
