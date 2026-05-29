package rabobank.parser

import rabobank.model.StatementRecord
import java.io.Reader

interface RecordParser {
    fun parse(reader: Reader, source: String = ""): Sequence<StatementRecord>
}
