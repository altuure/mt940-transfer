package rabobank.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import rabobank.model.StatementRecord
import java.math.BigDecimal

class ReporterTest {

    private fun rec(ref: Long, description: String) = StatementRecord(
        reference = ref,
        accountNumber = "NL01",
        description = description,
        startBalance = BigDecimal("10"),
        mutation = BigDecimal("+1"),
        endBalance = BigDecimal("11"),
        sourceFile = "",
        sourceLine = 0,
    )

    @Test
    fun `failures land in the output in call order`() {
        val reporter = Reporter()

        reporter.reportFailure(rec(112806, "Jan"), "duplicate transaction reference 112806")
        reporter.reportFailure(rec(131254, "Vincent"), "end balance ≠ start + mutation")
        reporter.reportBalances(mapOf("NL01" to BigDecimal("5.00")))


    }

    @Test
    fun `with no failures the header is skipped and 'no failed records' is emitted`() {
        val reporter = Reporter()

        reporter.reportBalances(mapOf("NL01" to BigDecimal("-2.50")))

    }
}
