# Project Context

## Purpose
ETL (Extract-Transform-Load) application for importing temperature data from CSV files into a MySQL database using Spring Batch. The application extracts specific columns (name, datetime, temp) from CSV files, handles duplicate entries gracefully, and provides job execution summaries.

## Tech Stack
- Java 21 (use Java 21 compatible features)
- Spring Boot 4.0.1
- Spring Batch (for batch processing)
- Spring JDBC (for database access)
- MySQL (production database)
- Testcontainers (for integration testing with MySQL)
- Maven (build tool)

## Project Conventions

### Code Style
- Use Java Records instead of POJOs for data transfer objects and immutable data structures
- Follow standard Java naming conventions (camelCase for methods/variables, PascalCase for classes)
- Prefer immutability where possible
- Use meaningful, descriptive names

### Architecture Patterns
- Spring Batch job architecture:
  - Reader: Read CSV files using FlatFileItemReader
  - Processor: Transform and validate data (optional)
  - Writer: Write to MySQL using JDBC
- Use Spring's dependency injection
- Separate configuration classes for batch job setup
- Use listeners for job/step events and reporting

### Testing Strategy
- **Integration tests**: Use Testcontainers with MySQL container (do NOT use H2 or other in-memory databases)
- **Unit tests**: Test individual components in isolation
- All tests must be reproducible and independent
- Test duplicate handling scenarios
- Test job completion summaries

### Git Workflow
- Feature branches for new functionality
- Conventional commits preferred
- PR reviews before merging

## Domain Context

### CSV Data Format
- Input: CSV files containing temperature readings
- Required columns to extract:
  - `name` - Location/sensor name (String)
  - `datetime` - Timestamp of the reading (LocalDateTime)
  - `temp` - Temperature value (Double/BigDecimal)
- Other columns in the CSV should be ignored

### Data Uniqueness
- The combination of `name` and `datetime` forms a unique identifier
- Duplicate entries (same name + datetime) must be:
  - Detected and reported
  - Skipped (not inserted into database)
  - Counted in job summary

### Job Summary
- After job completion, print summary including:
  - Total records processed
  - Records successfully inserted
  - Duplicate records skipped
  - Any errors encountered

## Important Constraints
- **No H2 database**: Must use MySQL for all testing via Testcontainers
- **Java 21 features**: Leverage modern Java features (records, pattern matching, etc.)
- **Duplicate handling**: Must gracefully handle duplicates without failing the job
- **Idempotent imports**: Running the same CSV twice should not create duplicate data

## External Dependencies
- MySQL database (containerized for testing, external for production)
- CSV files as input source (file system access required)
