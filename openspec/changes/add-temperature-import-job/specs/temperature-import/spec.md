# Temperature Import Capability

## ADDED Requirements

### Requirement: CSV Data Extraction
The system SHALL read temperature data from CSV files and extract only the `name`, `datetime`, and `temp` columns, ignoring all other columns present in the file.

#### Scenario: Extract required columns from CSV
- **WHEN** a CSV file is provided with columns including name, datetime, temp, and additional columns
- **THEN** only the name, datetime, and temp values are extracted
- **AND** all other columns are ignored

#### Scenario: Handle CSV with header row
- **WHEN** a CSV file contains a header row
- **THEN** the header row is skipped during processing
- **AND** data extraction begins from the first data row

### Requirement: Temperature Data Model
The system SHALL represent temperature readings using a Java Record with fields: name (String), datetime (LocalDateTime), and temp (Double).

#### Scenario: Create temperature data record
- **WHEN** temperature data is read from CSV
- **THEN** a TemperatureData record is created with the extracted values

### Requirement: Database Persistence
The system SHALL persist temperature data to a MySQL database using JDBC batch operations.

#### Scenario: Insert temperature records
- **WHEN** valid temperature data is processed
- **THEN** the data is inserted into the temperature_data table
- **AND** batch operations are used for efficient database writes

### Requirement: Unique Constraint Enforcement
The system SHALL enforce uniqueness on the combination of `name` and `datetime` columns in the database.

#### Scenario: Unique constraint on name and datetime
- **WHEN** the database schema is created
- **THEN** a unique constraint exists on (name, datetime) columns
- **AND** duplicate entries with the same name and datetime cannot be inserted

### Requirement: Duplicate Detection and Handling
The system SHALL detect duplicate entries (same name + datetime combination) and skip them without failing the job.

#### Scenario: Skip duplicate entry
- **WHEN** a record with the same name and datetime already exists in the database
- **THEN** the duplicate record is skipped
- **AND** the job continues processing remaining records
- **AND** the skipped duplicate is counted for reporting

#### Scenario: Report duplicate entries
- **WHEN** duplicate entries are detected during import
- **THEN** each duplicate is reported/logged
- **AND** the total count of duplicates is tracked

### Requirement: Job Execution Summary
The system SHALL print a summary report after job completion containing statistics about the import operation.

#### Scenario: Display job summary on completion
- **WHEN** the import job completes (successfully or with skips)
- **THEN** a summary is printed containing:
  - Total records processed
  - Records successfully inserted
  - Duplicate records skipped
  - Any errors encountered

#### Scenario: Summary with no duplicates
- **WHEN** the import job completes with no duplicate entries
- **THEN** the summary shows duplicates skipped as zero
- **AND** total processed equals records inserted

#### Scenario: Summary with duplicates
- **WHEN** the import job completes with some duplicate entries
- **THEN** the summary shows the count of duplicates skipped
- **AND** total processed equals records inserted plus duplicates skipped

### Requirement: Idempotent Import
The system SHALL support idempotent imports where running the same CSV file multiple times does not create duplicate data.

#### Scenario: Re-import same CSV file
- **WHEN** the same CSV file is imported a second time
- **THEN** all records are detected as duplicates
- **AND** no new records are inserted
- **AND** the job completes successfully with appropriate duplicate count

### Requirement: Spring Batch Job Architecture
The system SHALL implement the ETL job using Spring Batch framework with reader, processor (optional), and writer components.

#### Scenario: Job uses FlatFileItemReader
- **WHEN** the job is configured
- **THEN** a FlatFileItemReader is used to read CSV files

#### Scenario: Job uses JdbcBatchItemWriter
- **WHEN** the job is configured
- **THEN** a JdbcBatchItemWriter is used for database persistence

#### Scenario: Job has skip policy for duplicates
- **WHEN** the job is configured
- **THEN** a skip policy allows DataIntegrityViolationException to be skipped
- **AND** the job continues processing after encountering duplicates

### Requirement: Integration Testing with Testcontainers
The system SHALL have integration tests that use Testcontainers with MySQL (not H2 or other in-memory databases).

#### Scenario: Integration tests use MySQL container
- **WHEN** integration tests are executed
- **THEN** a MySQL Testcontainer is used as the database
- **AND** no H2 or in-memory database is used

#### Scenario: Test successful import
- **WHEN** a valid CSV file is processed in integration test
- **THEN** all records are inserted into the database
- **AND** the job completes successfully

#### Scenario: Test duplicate handling
- **WHEN** a CSV file with duplicates is processed in integration test
- **THEN** duplicates are skipped
- **AND** the job completes successfully with correct duplicate count
