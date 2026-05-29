package rabobank.processor

sealed interface ProcessResult

object ProcessSuccessResult : ProcessResult

data class ProcessFailResult(val message: String) : ProcessResult
