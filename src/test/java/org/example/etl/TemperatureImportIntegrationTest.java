package org.example.etl;

import org.example.etl.listener.DuplicateSkipListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the temperature import batch job using Testcontainers MySQL.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class TemperatureImportIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job temperatureImportJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DuplicateSkipListener duplicateSkipListener;

    @BeforeEach
    void setUp() {
        // Clean the temperature_data table before each test
        jdbcTemplate.execute("DELETE FROM temperature_data");
        duplicateSkipListener.reset();
    }

    @Test
    void testSuccessfulImport() throws Exception {
        // Given: test-data.csv with 5 unique records (default in application-test.properties)
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "classpath:test-data.csv")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When: the job is executed
        JobExecution execution = jobLauncher.run(temperatureImportJob, params);

        // Then: job completes successfully
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // And: all 5 records are inserted
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM temperature_data", Integer.class);
        assertThat(count).isEqualTo(5);

        // And: no duplicates were skipped
        assertThat(duplicateSkipListener.getSkipCount()).isZero();
    }

    @Test
    void testDuplicateHandling() throws Exception {
        // Given: test-duplicates.csv with 5 records including 2 duplicates
        // (Location-A + 2024-01-15 10:00:00 appears twice, Location-B + 2024-01-15 10:00:00 appears twice)
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "classpath:test-duplicates.csv")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When: the job is executed
        JobExecution execution = jobLauncher.run(temperatureImportJob, params);

        // Then: job completes successfully (doesn't fail on duplicates)
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // And: only 3 unique records are inserted
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM temperature_data", Integer.class);
        assertThat(count).isEqualTo(3);

        // And: 2 duplicates were skipped
        assertThat(duplicateSkipListener.getSkipCount()).isEqualTo(2);
    }

    @Test
    void testIdempotentImport() throws Exception {
        // Given: first import of test-data.csv
        JobParameters params1 = new JobParametersBuilder()
                .addString("inputFile", "classpath:test-data.csv")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution1 = jobLauncher.run(temperatureImportJob, params1);
        assertThat(execution1.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Integer countAfterFirst = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM temperature_data", Integer.class);
        assertThat(countAfterFirst).isEqualTo(5);

        duplicateSkipListener.reset();

        // When: the same file is imported again
        JobParameters params2 = new JobParametersBuilder()
                .addString("inputFile", "classpath:test-data.csv")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution2 = jobLauncher.run(temperatureImportJob, params2);

        // Then: job completes successfully
        assertThat(execution2.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // And: no new records are inserted (still 5)
        Integer countAfterSecond = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM temperature_data", Integer.class);
        assertThat(countAfterSecond).isEqualTo(5);

        // And: all 5 records were detected as duplicates
        assertThat(duplicateSkipListener.getSkipCount()).isEqualTo(5);
    }

    @Test
    void testExtractsOnlyRequiredColumns() throws Exception {
        // Given: test-data.csv has extra columns (humidity, pressure)
        JobParameters params = new JobParametersBuilder()
                .addString("inputFile", "classpath:test-data.csv")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When: the job is executed
        JobExecution execution = jobLauncher.run(temperatureImportJob, params);

        // Then: job completes successfully
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // And: only name, datetime, temp columns are stored
        var result = jdbcTemplate.queryForMap(
                "SELECT name, datetime, temp FROM temperature_data WHERE name = 'Location-A' AND temp = 22.5");
        assertThat(result.get("name")).isEqualTo("Location-A");
        assertThat(result.get("temp")).isEqualTo(22.5);
        assertThat(result.get("datetime")).isNotNull();
    }
}
