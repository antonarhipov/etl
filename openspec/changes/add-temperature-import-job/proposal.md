# Change: Add Temperature Import ETL Job

## Why
The application needs a complete ETL job to import temperature data from CSV files into MySQL database. This is the core functionality of the ETL application that enables automated batch processing of temperature readings with proper duplicate handling and job execution reporting.

## What Changes
- Add `TemperatureData` Java Record to represent temperature readings (name, datetime, temp)
- Implement Spring Batch job configuration with:
  - FlatFileItemReader for CSV parsing (extracting only name, datetime, temp columns)
  - ItemProcessor for data transformation and validation
  - JdbcBatchItemWriter for MySQL database persistence
- Add database schema for temperature_data table with unique constraint on (name, datetime)
- Implement duplicate detection and skip logic with reporting
- Add job execution listeners for summary reporting (processed/inserted/skipped/errors)
- Create comprehensive integration tests using Testcontainers with MySQL

## Impact
- Affected specs: `temperature-import` (new capability)
- Affected code:
  - `src/main/java/org/example/etl/model/TemperatureData.java` - New record
  - `src/main/java/org/example/etl/config/BatchConfiguration.java` - Job configuration
  - `src/main/java/org/example/etl/listener/ImportJobListener.java` - Job listener
  - `src/main/java/org/example/etl/listener/DuplicateSkipListener.java` - Skip listener
  - `src/main/resources/schema.sql` - Database schema
  - `src/test/java/org/example/etl/` - Integration tests
