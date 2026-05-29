package rabobank

import org.slf4j.LoggerFactory
import rabobank.parser.CsvParser
import rabobank.parser.RecordParser
import rabobank.parser.XmlParser
import rabobank.processor.AccountBalanceCollector
import rabobank.processor.BalanceProcessor
import rabobank.processor.Pipeline
import rabobank.processor.ProcessFailResult
import rabobank.processor.ProcessSuccessResult
import rabobank.processor.UniqueReferenceProcessor
import rabobank.report.Reporter
import java.io.File
import java.nio.charset.Charset
import kotlin.system.exitProcess
import kotlin.text.Charsets.ISO_8859_1
import kotlin.text.Charsets.UTF_8

class Main {
    private val log = LoggerFactory.getLogger(Main::class.java)
    val reporter: Reporter = Reporter()

    fun run(args: Array<String>) {
        val paths = if (args.isEmpty()) DEFAULT_FILES else args.toList()
        log.info("Starting with {} input file(s): {}", paths.size, paths)

        val collector = AccountBalanceCollector()
        val pipeline = Pipeline(
            listOf(
                UniqueReferenceProcessor(),
                BalanceProcessor(),
                collector,
            )
        )


        for (path in paths) {
            processFile(File(path), pipeline, reporter)
        }

        reporter.reportBalances(collector.accountBalances)
        log.info("Finished. {} account(s) aggregated.", collector.accountBalances.size)
    }

    private fun processFile(file: File, pipeline: Pipeline, reporter: Reporter) {
        val (parser, charset) = parserFor(file)
        log.info("Processing {} as {} ({})", file.path, file.extension.lowercase(), charset)
        var count = 0
        file.reader(charset).use { reader ->
            for (record in parser.parse(reader, file.name)) {
                count++
                when (val r = pipeline.process(record)) {
                    is ProcessFailResult -> reporter.reportFailure(record, r.message)
                    ProcessSuccessResult -> Unit
                }
            }
        }
        log.info("Processed {} record(s) from {}", count, file.path)
    }

    private fun parserFor(file: File): Pair<RecordParser, Charset> =
        when (file.extension.lowercase()) {
            "csv" -> CsvParser() to ISO_8859_1
            "xml" -> XmlParser() to UTF_8
            else -> {
                log.error("Unsupported file type: {}. Expected .csv or .xml.", file.name)
                System.err.println(
                    "Unsupported file type: ${file.name}. Expected .csv or .xml."
                )
                exitProcess(1)
            }
        }

    companion object {
        private val DEFAULT_FILES = listOf(
            "assignment-candidate/records.csv",
            "assignment-candidate/records.xml",
        )

        @JvmStatic
        fun main(args: Array<String>) {
            Main().run(args)
        }
    }
}
