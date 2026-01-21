# Tasks: Add Temperature Import ETL Job

## 1. Data Model
- [x] 1.1 Create `TemperatureData` Java Record with fields: name (String), datetime (LocalDateTime), temp (Double)

## 2. Database Schema
- [x] 2.1 Create `schema.sql` with temperature_data table
- [x] 2.2 Add unique constraint on (name, datetime) columns
- [x] 2.3 Configure Spring Boot to initialize schema

## 3. Spring Batch Job Configuration
- [x] 3.1 Create `BatchConfiguration` class with job bean
- [x] 3.2 Implement `FlatFileItemReader` for CSV parsing
  - [x] 3.2.1 Configure to extract only name, datetime, temp columns
  - [x] 3.2.2 Handle CSV header row
  - [x] 3.2.3 Map to TemperatureData record
- [x] 3.3 Implement `JdbcBatchItemWriter` for MySQL persistence
- [x] 3.4 Configure step with reader and writer
- [x] 3.5 Add skip policy for duplicate key violations

## 4. Duplicate Handling
- [x] 4.1 Create `DuplicateSkipListener` to track skipped duplicates
- [x] 4.2 Configure skip policy for DataIntegrityViolationException
- [x] 4.3 Count and report duplicate entries

## 5. Job Summary Reporting
- [x] 5.1 Create `ImportJobListener` implementing JobExecutionListener
- [x] 5.2 Print summary on job completion:
  - [x] 5.2.1 Total records processed
  - [x] 5.2.2 Records successfully inserted
  - [x] 5.2.3 Duplicate records skipped
  - [x] 5.2.4 Errors encountered

## 6. Testing
- [x] 6.1 Set up Testcontainers MySQL configuration
- [x] 6.2 Create integration test for successful import
- [x] 6.3 Create integration test for duplicate handling
- [x] 6.4 Create test CSV files with sample data
- [x] 6.5 Verify job summary output

## 7. Application Configuration
- [x] 7.1 Configure application.properties for MySQL connection
- [x] 7.2 Configure job parameters (input file path)
