package rabobank.parser

import com.opencsv.CSVReaderBuilder
import org.slf4j.LoggerFactory
import rabobank.model.StatementRecord
import java.io.Reader
import java.math.BigDecimal

class CsvParser : RecordParser {
    private val log = LoggerFactory.getLogger(CsvParser::class.java)

    override fun parse(reader: Reader, source: String): Sequence<StatementRecord> {
        val csvReader = CSVReaderBuilder(reader)
            .withSkipLines(1) // header row
            .build()
        return generateSequence { csvReader.readNext() }
            .filter { row -> row.any { it.isNotBlank() } }
            .map { row -> toRecord(row.map { it.trim() }, source, csvReader.linesRead.toInt()) }
    }

    private fun toRecord(fields: List<String>, source: String, line: Int): StatementRecord {
        require(fields.size == 6) { "Expected 6 CSV columns, got ${fields.size}: $fields" }
        val record = StatementRecord(
            reference = fields[0].toLong(),
            accountNumber = fields[1],
            description = fields[2],
            startBalance = BigDecimal(fields[3]),
            mutation = BigDecimal(fields[4]),
            endBalance = BigDecimal(fields[5]),
            sourceFile = source,
            sourceLine = line,
        )
        log.debug("Parsed CSV record reference={} account={} at {}:{}", record.reference, record.accountNumber, source, line)
        return record
    }
}
