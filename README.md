# mt940-transfer

Kotlin implementation of the Rabobank Customer Statement Processor coding assignment. 

## Validations

1. **all transaction references should be unique**
2. **the end balance needs to be validated**

## Assumption 

1. Files are in valid format
2. transactions are accepted or rejects one by one in order (first entry for a reference will be accected, following entries for same reference will be rejected)

## Requirements

- JDK 8 or newer (`kotlin.compiler.jvmTarget` is `1.8`)
- Maven 3.6+

## Build, test, run

```bash
mvn compile                                  # compile
mvn test                                     # run all tests (JUnit 5 + AssertJ)
mvn exec:java                                # run against bundled samples
mvn package                                  # build jar into target/
```

`mvn exec:java` does not depend on the `compile` phase, so after editing source run `mvn compile exec:java`.
## CLI

```
mt940-transfer [<file> ...]
```

- Zero args: processes the two bundled sample files.
- One or more args: each file is processed in order. The parser is chosen by extension — `.csv` is read with `ISO_8859_1`, `.xml` with `UTF_8`.
- Any other extension prints `Unsupported file type: …` on stderr and exits with code 1.

Pass arguments through Maven via `-Dexec.args`:

```bash
mvn exec:java -Dexec.args="path/to/records.csv path/to/records.xml"
mvn exec:java -Dexec.args="january.csv february.csv march.xml"
```

Note: `mvn exec:java` intercepts `System.exit`, so a usage failure surfaces only as the stderr message under Maven. When run via `java -cp …`, exit code 64 is real.

## Example output

```
23:42:27.602 INFO  rabobank.Main - Starting with 2 input file(s): [assignment-candidate/records.csv, assignment-candidate/records.xml]
23:42:27.618 INFO  rabobank.Main - Processing assignment-candidate/records.csv as csv (ISO-8859-1)
23:42:27.637 INFO  r.p.UniqueReferenceProcessor - Duplicate reference detected: 112806
23:42:27.638 WARN  rabobank.report.Reporter - Failure at records.csv:6 ref=112806 "Subscription from Daniël Theuß" - duplicate transaction reference 112806
23:42:27.638 INFO  r.p.UniqueReferenceProcessor - Duplicate reference detected: 112806
23:42:27.638 WARN  rabobank.report.Reporter - Failure at records.csv:7 ref=112806 "Subscription for Rik Dekker" - duplicate transaction reference 112806
23:42:27.639 INFO  rabobank.Main - Processed 10 record(s) from assignment-candidate/records.csv
23:42:27.639 INFO  rabobank.Main - Processing assignment-candidate/records.xml as xml (UTF-8)
23:42:27.645 INFO  r.processor.BalanceProcessor - Balance mismatch ref=131254 expected=4490 actual=6368
23:42:27.645 WARN  rabobank.report.Reporter - Failure at records.xml:9 ref=131254 "Candy from Vincent de Vries" - end balance 6368 ≠ start 5429 + mutation -939 (expected 4490)
23:42:27.646 INFO  r.processor.BalanceProcessor - Balance mismatch ref=192480 expected=4980 actual=4981
23:42:27.646 WARN  rabobank.report.Reporter - Failure at records.xml:58 ref=192480 "Subscription for Erik de Vries" - end balance 4981 ≠ start 3980 + mutation 1000 (expected 4980)
23:42:27.647 INFO  rabobank.Main - Processed 10 record(s) from assignment-candidate/records.xml
23:42:27.647 INFO  rabobank.report.Reporter - Report complete: 4 failure(s), 8 account(s).
23:42:27.647 INFO  rabobank.Main - Finished. 8 account(s) aggregated.
…
```

Failures are emitted in input order (as records stream through the pipeline). Balances are sorted by IBAN.

## Architecture

End-to-end streaming: records flow from parser to processor chain to reporter one at a time. The only state retained across records is the unique-reference set and the per-account balance map; no intermediate list of records exists.

```
rabobank.model                                  model objects (StatementRecord,StatementError) 
rabobank.parser                                 Parser interface and implementations for CSV and XML, All implementations are memory efficient.
rabobank.processor                              Input process logic including process chain (Pipeline)
    RecordProcessor,ProcessorChain              Basic interface to extend for validator and storage
    ProcessResult                               Simple result of record processing (ProcessSuccessResult,ProcessFailResult)
    UniqueReferenceProcessor                    Validates is statement is unique and only accepts after all process
    BalanceProcessor                            validate end balance within statement
    AccountBalanceCollector                     Simple balance collector for end storage
rabobank.report                                 End Summary reporter for audit.
```

The processor pipeline is modeled on `javax.servlet.FilterChain`: each `RecordProcessor` receives the record and a `ProcessorChain`, and either returns its own `ProcessFailResult(message)` or returns whatever `chain.proceed(record)` returned. Failure short-circuits because the failing processor never calls `proceed` — no exceptions are involved.

`UniqueReferenceProcessor` lets the first occurrence of a reference through and rejects subsequent ones. This is the natural filter-chain semantics and differs from a "flag all copies including the first" reading of the spec.


## TODO

* File input validation and error handling
  * There is no input validation for now, it assumes, file format is correct for both XML and CSV
* Konsist/Archunit like architecture validation
