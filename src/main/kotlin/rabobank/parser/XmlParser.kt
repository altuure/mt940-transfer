package rabobank.parser

import org.slf4j.LoggerFactory
import rabobank.model.StatementRecord
import java.io.Reader
import java.math.BigDecimal
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants.END_ELEMENT
import javax.xml.stream.XMLStreamConstants.START_ELEMENT
import javax.xml.stream.XMLStreamReader

class XmlParser : RecordParser {
    private val log = LoggerFactory.getLogger(XmlParser::class.java)

    override fun parse(reader: Reader, source: String): Sequence<StatementRecord> = sequence {
        val xr = XMLInputFactory.newFactory().createXMLStreamReader(reader)
        try {
            while (xr.hasNext()) {
                if (xr.next() == START_ELEMENT && xr.localName == "record") {
                    val line = xr.location.lineNumber
                    val record = readRecord(xr, source, line)
                    log.debug("Parsed XML record reference={} account={} at {}:{}", record.reference, record.accountNumber, source, line)
                    yield(record)
                }
            }
        } finally {
            xr.close()
        }
    }

    private fun readRecord(xr: XMLStreamReader, source: String, line: Int): StatementRecord {
        val reference = xr.getAttributeValue(null, "reference").toLong()
        var accountNumber = ""
        var description = ""
        var startBalance = ""
        var mutation = ""
        var endBalance = ""

        while (xr.hasNext()) {
            when (xr.next()) {
                START_ELEMENT -> {
                    val tag = xr.localName
                    val text = xr.elementText.trim()
                    when (tag) {
                        "accountNumber" -> accountNumber = text
                        "description" -> description = text
                        "startBalance" -> startBalance = text
                        "mutation" -> mutation = text
                        "endBalance" -> endBalance = text
                    }
                }
                END_ELEMENT -> if (xr.localName == "record") {
                    return StatementRecord(
                        reference = reference,
                        accountNumber = accountNumber,
                        description = description,
                        startBalance = BigDecimal(startBalance),
                        mutation = BigDecimal(mutation),
                        endBalance = BigDecimal(endBalance),
                        sourceFile = source,
                        sourceLine = line,
                    )
                }
            }
        }
        error("Unterminated <record reference=\"$reference\">")
    }
}
