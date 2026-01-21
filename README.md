# Temperature Import ETL Application

A Spring Batch-based ETL (Extract-Transform-Load) application for importing temperature data from CSV files into a MySQL database.

## Features

- **CSV Data Extraction**: Reads CSV files and extracts specific columns (`name`, `datetime`, `temp`), ignoring additional columns
- **Batch Processing**: Efficient batch operations using Spring Batch framework
- **Duplicate Handling**: Gracefully handles duplicate entries (same `name` + `datetime`) by skipping them without failing the job
- **Idempotent Imports**: Running the same CSV file multiple times does not create duplicate data
- **Job Summary Reporting**: Provides comprehensive job execution summary including:
  - Total records processed
  - Records successfully inserted
  - Duplicate records skipped
  - Any errors encountered

## Tech Stack

- Java 21
- Spring Boot 4.0.1
- Spring Batch
- Spring JDBC
- MySQL 8.0
- Maven
- Testcontainers (for integration testing)

## Prerequisites

- Java 21 or higher
- Maven 3.9+
- MySQL 8.0 (for production)
- Docker (for running tests with Testcontainers)

## Project Structure

```
src/
├── main/
│   ├── java/org/example/etl/
│   │   ├── EtlApplication.java          # Main application entry point
│   │   ├── config/
│   │   │   └── BatchConfiguration.java  # Spring Batch job configuration
│   │   ├── listener/
│   │   │   ├── DuplicateSkipListener.java   # Tracks skipped duplicates
│   │   │   └── ImportJobListener.java       # Job summary reporter
│   │   └── model/
│   │       └── TemperatureData.java     # Temperature data record
│   └── resources/
│       ├── application.properties       # Application configuration
│       └── schema.sql                   # Database schema
└── test/
    ├── java/org/example/etl/
    │   ├── TemperatureImportIntegrationTest.java  # Integration tests
    │   ├── TestcontainersConfiguration.java       # Testcontainers setup
    │   └── listener/
    │       ├── DuplicateSkipListenerTest.java
    │       └── ImportJobListenerTest.java
    └── resources/
        ├── application-test.properties
        └── test-*.csv                   # Test data files
```

## Database Schema

The application uses a `temperature_data` table with the following structure:

```sql
CREATE TABLE temperature_data (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    datetime TIMESTAMP NOT NULL,
    temp DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_name_datetime UNIQUE (name, datetime)
);
```

## Configuration

### Application Properties

Configure the following properties in `application.properties`:

```properties
# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/etl_db
spring.datasource.username=root
spring.datasource.password=root

# Input file location
etl.input.file=classpath:data.csv
```

### CSV File Format

The input CSV file should contain at minimum the following columns:
- `name` - Location or sensor name (String)
- `datetime` - Timestamp in format `yyyy-MM-dd HH:mm:ss`
- `temp` - Temperature value (numeric)

Example:
```csv
name,datetime,temp,humidity,pressure
Location-A,2024-01-15 10:00:00,22.5,45.0,1013.25
Location-B,2024-01-15 10:00:00,18.3,52.0,1012.80
```

**Note**: Additional columns (like `humidity`, `pressure`) are automatically ignored.

## Usage

### Building the Application

```bash
./mvnw clean package
```

### Running the Application

1. **Start MySQL database** (or use Docker):
   ```bash
   docker run -d --name mysql-etl \
     -e MYSQL_ROOT_PASSWORD=root \
     -e MYSQL_DATABASE=etl_db \
     -p 3306:3306 \
     mysql:8.0
   ```

2. **Run the application with a specific input file**:
   ```bash
   java -jar target/etl-0.0.1-SNAPSHOT.jar --inputFile=file:/path/to/your/data.csv
   ```

   Or using Maven:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--inputFile=file:/path/to/your/data.csv"
   ```

### Job Execution Summary

After the job completes, a summary is printed to the console:

```
===========================================
       TEMPERATURE IMPORT JOB SUMMARY      
===========================================
Status:                    COMPLETED
Total records processed:   100
Records inserted:          95
Duplicates skipped:        5
Total skipped:             5
Errors encountered:        0
===========================================
```

## Testing

### Running All Tests

```bash
./mvnw test
```

### Running Tests with Ryuk Disabled (CI environments)

```bash
TESTCONTAINERS_RYUK_DISABLED=true ./mvnw test
```

### Running Specific Tests

```bash
# Unit tests only
./mvnw test -Dtest=TemperatureDataTest,DuplicateSkipListenerTest,ImportJobListenerTest

# Integration tests only
./mvnw test -Dtest=TemperatureImportIntegrationTest
```

### Test Coverage

- **Unit Tests**: Test individual components (model, listeners)
- **Integration Tests**: Test the complete job execution with MySQL Testcontainer
  - Successful import
  - Duplicate handling
  - Idempotent imports
  - Column extraction

## Architecture

The application follows the standard Spring Batch architecture:

```
┌─────────────┐    ┌───────────────────┐    ┌─────────────────┐
│   CSV File  │───▶│ FlatFileItemReader│───▶│ JdbcBatchItemWriter│───▶ MySQL
└─────────────┘    └───────────────────┘    └─────────────────┘
                            │                        │
                            ▼                        ▼
                   ┌─────────────────┐      ┌───────────────────┐
                   │ Skip duplicate  │      │ DuplicateSkipListener│
                   │ on read error   │      │ (tracks skips)    │
                   └─────────────────┘      └───────────────────┘
                                                     │
                                                     ▼
                                           ┌───────────────────┐
                                           │ ImportJobListener │
                                           │ (prints summary)  │
                                           └───────────────────┘
```

### Key Components

| Component | Description |
|-----------|-------------|
| `TemperatureData` | Java Record representing a temperature reading |
| `BatchConfiguration` | Configures the Spring Batch job, reader, writer, and step |
| `DuplicateSkipListener` | Tracks and logs skipped duplicate entries |
| `ImportJobListener` | Prints job execution summary on completion |

## License

This project is for demonstration purposes.
