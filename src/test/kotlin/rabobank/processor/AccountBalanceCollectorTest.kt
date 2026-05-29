package rabobank.processor

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import rabobank.model.StatementRecord
import java.math.BigDecimal

class AccountBalanceCollectorTest {

    private fun rec(account: String, mutation: String) = StatementRecord(
        reference = 1,
        accountNumber = account,
        description = "x",
        startBalance = BigDecimal("0.00"),
        mutation = BigDecimal(mutation),
        endBalance = BigDecimal(mutation),
        sourceFile = "",
        sourceLine = 0,
    )

    @Test
    fun `sums mutations per account`() {
        val collector = AccountBalanceCollector()
        val pipeline = Pipeline(listOf(collector))

        pipeline.process(rec("NL01", "+10.00"))
        pipeline.process(rec("NL02", "+5.00"))
        pipeline.process(rec("NL01", "-3.50"))
        pipeline.process(rec("NL01", "+0.50"))

        assertThat(collector.accountBalances)
            .hasSize(2)
            .containsEntry("NL01", BigDecimal("7.00"))
            .containsEntry("NL02", BigDecimal("5.00"))
    }
}
