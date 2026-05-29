package rabobank.processor

import org.slf4j.LoggerFactory
import rabobank.model.StatementRecord

class Pipeline(private val processors: List<RecordProcessor>) {
    private val log = LoggerFactory.getLogger(Pipeline::class.java)

    init {
        log.debug("Pipeline built with {} processor(s): {}", processors.size, processors.map { it::class.simpleName })
    }

    fun process(record: StatementRecord): ProcessResult = chainAt(0).proceed(record)

    private fun chainAt(index: Int): ProcessorChain = object : ProcessorChain {
        override fun proceed(record: StatementRecord): ProcessResult =
            if (index < processors.size) {
                processors[index].process(record, chainAt(index + 1))
            } else {
                ProcessSuccessResult
            }
    }
}
