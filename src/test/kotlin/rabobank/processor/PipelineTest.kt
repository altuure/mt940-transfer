package rabobank.processor

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import rabobank.model.StatementRecord
import java.math.BigDecimal

class PipelineTest {

    private fun rec(ref: Long, start: String = "10", mutation: String = "+1", end: String = "11") =
        StatementRecord(
            reference = ref,
            accountNumber = "NL01",
            description = "r=$ref",
            startBalance = BigDecimal(start),
            mutation = BigDecimal(mutation),
            endBalance = BigDecimal(end),
            sourceFile = "",
            sourceLine = 0,
        )

    @Test
    fun `failing processor short-circuits the rest of the chain`() {
        val collector = AccountBalanceCollector()
        val pipeline = Pipeline(
            listOf(UniqueReferenceProcessor(), BalanceProcessor(), collector)
        )

        assertThat(pipeline.process(rec(1))).isSameAs(ProcessSuccessResult)
        assertThat(pipeline.process(rec(2, start = "10", mutation = "+1", end = "99")))
            .isInstanceOf(ProcessFailResult::class.java)

        assertThat(collector.accountBalances)
            .hasSize(1)
            .containsEntry("NL01", BigDecimal("1"))
    }

    @Test
    fun `processors run in registered order`() {
        val calls = mutableListOf<String>()
        val a = object : RecordProcessor {
            override fun process(record: StatementRecord, chain: ProcessorChain): ProcessResult {
                calls += "a"; return chain.proceed(record)
            }
        }
        val b = object : RecordProcessor {
            override fun process(record: StatementRecord, chain: ProcessorChain): ProcessResult {
                calls += "b"; return chain.proceed(record)
            }
        }

        assertThat(Pipeline(listOf(a, b)).process(rec(1))).isSameAs(ProcessSuccessResult)
        assertThat(calls).containsExactly("a", "b")
    }

    @Test
    fun `empty pipeline returns success`() {
        assertThat(Pipeline(emptyList()).process(rec(1))).isSameAs(ProcessSuccessResult)
    }
}
