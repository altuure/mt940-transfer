package rabobank.model

import java.math.BigDecimal

data class StatementRecord(
    val reference: Long,
    val accountNumber: String,
    val description: String,
    val startBalance: BigDecimal,
    val mutation: BigDecimal,
    val endBalance: BigDecimal,
    val sourceFile: String ,
    val sourceLine: Int,
)
