package rabobank.processor

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import rabobank.model.StatementRecord
import java.math.BigDecimal

class BalanceProcessorTest {

    private fun rec(start: String, mutation: String, end: String) = StatementRecord(
        reference = 1,
        accountNumber = "NL00BANK0000000000",
        description = "x",
        startBalance = BigDecimal(start),
        mutation = BigDecimal(mutation),
        endBalance = BigDecimal(end),
        sourceFile = "",
        sourceLine = 0,
    )

    private class Sink : RecordProcessor {
        var advanced = false
        override fun process(record: StatementRecord, chain: ProcessorChain): ProcessResult {
            advanced = true
            return chain.proceed(record)
        }
    }

    @Test
    fun `succeeds when start plus mutation equals end`() {
        val sink = Sink()

        val result = Pipeline(listOf(BalanceProcessor(), sink))
            .process(rec("33.34", "+5.55", "38.89"))

        assertThat(result).isSameAs(ProcessSuccessResult)
        assertThat(sink.advanced).isTrue()
    }

    @Test
    fun `succeeds across scale differences`() {
        val sink = Sink()

        val result = Pipeline(listOf(BalanceProcessor(), sink))
            .process(rec("10.0", "+0.50", "10.50"))

        assertThat(result).isSameAs(ProcessSuccessResult)
        assertThat(sink.advanced).isTrue()
    }

    @Test
    fun `fails with message and halts chain when balance is wrong`() {
        val sink = Sink()

        val result = Pipeline(listOf(BalanceProcessor(), sink))
            .process(rec("13.89", "-46.18", "99.99"))

        assertThat(result).isInstanceOf(ProcessFailResult::class.java)
        val fail = result as ProcessFailResult
        assertThat(fail.message).contains("end balance", "99.99")
        assertThat(sink.advanced).isFalse()
    }
}
