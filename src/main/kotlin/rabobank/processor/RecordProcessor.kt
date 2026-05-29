package rabobank.processor

import rabobank.model.StatementRecord

interface RecordProcessor {
    fun process(record: StatementRecord, chain: ProcessorChain): ProcessResult
}

interface ProcessorChain {
    fun proceed(record: StatementRecord): ProcessResult
}
