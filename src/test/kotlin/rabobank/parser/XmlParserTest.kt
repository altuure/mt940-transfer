package rabobank.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class XmlParserTest {

    @Test
    fun `parses records and attribute references`() {
        val xml = """
            <records>
              <record reference="138932">
                <accountNumber>NL90ABNA0585647886</accountNumber>
                <description>Flowers for Richard Bakker</description>
                <startBalance>94.9</startBalance>
                <mutation>+14.63</mutation>
                <endBalance>109.53</endBalance>
              </record>
              <record reference="131254">
                <accountNumber>NL93ABNA0585619023</accountNumber>
                <description>Candy from Vincent de Vries</description>
                <startBalance>5429</startBalance>
                <mutation>-939</mutation>
                <endBalance>6368</endBalance>
              </record>
            </records>
        """.trimIndent()

        val records = XmlParser().parse(xml.reader()).toList()

        assertThat(records).hasSize(2)
        val first = records[0]
        assertThat(first.reference).isEqualTo(138932L)
        assertThat(first.description).isEqualTo("Flowers for Richard Bakker")
        assertThat(first.startBalance).isEqualTo(BigDecimal("94.9"))
        assertThat(first.mutation).isEqualTo(BigDecimal("14.63"))
        assertThat(first.endBalance).isEqualTo(BigDecimal("109.53"))
        assertThat(first.sourceLine).isEqualTo(2)
        assertThat(records[1].mutation).isEqualTo(BigDecimal("-939"))
    }

    @Test
    fun `streams lazily — first does not force the rest`() {
        val xml = """
            <records>
              <record reference="1">
                <accountNumber>NL00BANK0000000000</accountNumber>
                <description>ok</description>
                <startBalance>0</startBalance>
                <mutation>+0</mutation>
                <endBalance>0</endBalance>
              </record>
              <record reference="2">
                <accountNumber>BROKEN</accountNumber>
                <description>this record has a non-numeric balance</description>
                <startBalance>not-a-number</startBalance>
                <mutation>+0</mutation>
                <endBalance>0</endBalance>
              </record>
            </records>
        """.trimIndent()

        val first = XmlParser().parse(xml.reader()).first()

        assertThat(first.reference).isEqualTo(1L)
    }
}
