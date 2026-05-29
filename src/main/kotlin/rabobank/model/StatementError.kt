package rabobank.model

data class StatementError(
    val sourceFile: String,
    val sourceLine: Int,
    val errorMessage: String
)
