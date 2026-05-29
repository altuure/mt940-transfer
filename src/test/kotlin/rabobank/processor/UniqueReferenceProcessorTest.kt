package rabobank.processor

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import rabobank.model.StatementRecord
import java.math.BigDecimal

class UniqueReferenceProcessorTest {

    private fun rec(ref: Long, tag: String = "$ref") = StatementRecord(
        reference = ref,
        accountNumber = "NL00BANK0000000000",
        description = "ref=$ref tag=$tag",
        startBalance = BigDecimal("10.00"),
        mutation = BigDecimal("+1.00"),
        endBalance = BigDecimal("11.00"),
        sourceFile = "",
        sourceLine = 0,
    )

    private class CountingSink : RecordProcessor {
        var count = 0
        override fun process(record: StatementRecord, chain: ProcessorChain): ProcessResult {
            count++
            return chain.proceed(record)
        }
    }

    @Test
    fun `first occurrence of a reference proceeds`() {
        val sink = CountingSink()
        val pipeline = Pipeline(listOf(UniqueReferenceProcessor(), sink))

        assertThat(pipeline.process(rec(1))).isSameAs(ProcessSuccessResult)
        assertThat(pipeline.process(rec(2))).isSameAs(ProcessSuccessResult)
        assertThat(sink.count).isEqualTo(2)
    }

    @Test
    fun `second occurrence fails with a message and does not advance the chain`() {
        val sink = CountingSink()
        val pipeline = Pipeline(listOf(UniqueReferenceProcessor(), sink))

        pipeline.process(rec(1, "a"))
        val result = pipeline.process(rec(1, "b"))

        assertThat(result).isInstanceOf(ProcessFailResult::class.java)
        val fail = result as ProcessFailResult
        assertThat(fail.message).contains("duplicate", "1")
        assertThat(sink.count).isEqualTo(1)
    }
}
