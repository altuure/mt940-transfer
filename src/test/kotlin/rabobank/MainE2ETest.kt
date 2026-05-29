package rabobank

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Test

class MainE2ETest {

    @Test
    fun `csv file alone collects duplicate-reference failures at the right lines`() {
        val main = Main()

        main.run(arrayOf("assignment-candidate/records.csv"))

        val errors = main.reporter.statementErrors
        assertThat(errors).hasSize(2)
        assertThat(errors[0].sourceFile).isEqualTo("records.csv")
        assertThat(errors[0].sourceLine).isEqualTo(6)
        assertThat(errors[0].errorMessage).isEqualTo("duplicate transaction reference 112806")
        assertThat(errors[1].sourceFile).isEqualTo("records.csv")
        assertThat(errors[1].sourceLine).isEqualTo(7)
        assertThat(errors[1].errorMessage).isEqualTo("duplicate transaction reference 112806")
    }

    @Test
    fun `xml file alone collects balance-mismatch failures at the right lines`() {
        val main = Main()

        main.run(arrayOf("assignment-candidate/records.xml"))

        val errors = main.reporter.statementErrors
        assertThat(errors).hasSize(2)
        assertThat(errors[0].sourceFile).isEqualTo("records.xml")
        assertThat(errors[0].sourceLine).isEqualTo(9)
        assertThat(errors[0].errorMessage)
            .isEqualTo("end balance 6368 ≠ start 5429 + mutation -939 (expected 4490)")
        assertThat(errors[1].sourceFile).isEqualTo("records.xml")
        assertThat(errors[1].sourceLine).isEqualTo(58)
        assertThat(errors[1].errorMessage)
            .isEqualTo("end balance 4981 ≠ start 3980 + mutation 1000 (expected 4980)")
    }

    @Test
    fun `processing both files reports failures from each in input order`() {
        val main = Main()

        main.run(
            arrayOf(
                "assignment-candidate/records.csv",
                "assignment-candidate/records.xml",
            )
        )

        assertThat(main.reporter.statementErrors)
            .extracting("sourceFile", "sourceLine", "errorMessage")
            .containsExactly(
                tuple("records.csv", 6, "duplicate transaction reference 112806"),
                tuple("records.csv", 7, "duplicate transaction reference 112806"),
                tuple("records.xml", 9, "end balance 6368 ≠ start 5429 + mutation -939 (expected 4490)"),
                tuple("records.xml", 58, "end balance 4981 ≠ start 3980 + mutation 1000 (expected 4980)"),
            )
    }
}
