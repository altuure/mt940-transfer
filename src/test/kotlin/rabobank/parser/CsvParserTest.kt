package rabobank.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CsvParserTest {

    @Test
    fun `parses header and rows`() {
        val csv = """
            Reference,Account Number,Description,Start Balance,Mutation,End Balance
            183398,NL56RABO0149876948,Clothes from Richard de Vries,33.34,+5.55,38.89
            112806,NL27SNSB0917829871,Subscription from Jan Dekker,28.95,-19.44,9.51
        """.trimIndent()

        val records = CsvParser().parse(csv.reader()).toList()

        assertThat(records).hasSize(2)
        val first = records[0]
        assertThat(first.reference).isEqualTo(183398L)
        assertThat(first.accountNumber).isEqualTo("NL56RABO0149876948")
        assertThat(first.description).isEqualTo("Clothes from Richard de Vries")
        assertThat(first.startBalance).isEqualTo(BigDecimal("33.34"))
        assertThat(first.mutation).isEqualTo(BigDecimal("5.55"))
        assertThat(first.endBalance).isEqualTo(BigDecimal("38.89"))
        assertThat(first.sourceLine).isEqualTo(2)
        assertThat(records[1].mutation).isEqualTo(BigDecimal("-19.44"))
    }
}
