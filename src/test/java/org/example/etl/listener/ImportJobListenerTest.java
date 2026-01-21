package org.example.etl.listener;

import org.example.etl.model.TemperatureData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.step.StepExecution;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ImportJobListener.
 */
class ImportJobListenerTest {

    private DuplicateSkipListener duplicateSkipListener;
    private ImportJobListener importJobListener;

    @BeforeEach
    void setUp() {
        duplicateSkipListener = new DuplicateSkipListener();
        importJobListener = new ImportJobListener(duplicateSkipListener);
    }

    @Test
    void testBeforeJobResetsSkipCounter() {
        // Given: some skips have been recorded
        TemperatureData data = new TemperatureData("Location-A", LocalDateTime.now(), 22.5);
        duplicateSkipListener.onSkipInWrite(data, new RuntimeException("test"));
        assertThat(duplicateSkipListener.getSkipCount()).isEqualTo(1);

        // Mock job execution
        JobExecution jobExecution = mock(JobExecution.class);
        JobInstance jobInstance = mock(JobInstance.class);
        when(jobExecution.getJobInstance()).thenReturn(jobInstance);
        when(jobInstance.getJobName()).thenReturn("testJob");

        // When
        importJobListener.beforeJob(jobExecution);

        // Then: skip counter should be reset
        assertThat(duplicateSkipListener.getSkipCount()).isZero();
    }

    @Test
    void testAfterJobPrintsSummary() {
        // Given
        JobExecution jobExecution = mock(JobExecution.class);
        StepExecution stepExecution = mock(StepExecution.class);

        when(jobExecution.getStepExecutions()).thenReturn(List.of(stepExecution));
        when(stepExecution.getReadCount()).thenReturn(100L);
        when(stepExecution.getWriteCount()).thenReturn(95L);
        when(stepExecution.getSkipCount()).thenReturn(5L);
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobExecution.getAllFailureExceptions()).thenReturn(List.of());

        // When/Then - no exception should be thrown
        importJobListener.afterJob(jobExecution);
    }

    @Test
    void testAfterJobWithFailures() {
        // Given
        JobExecution jobExecution = mock(JobExecution.class);
        StepExecution stepExecution = mock(StepExecution.class);

        when(jobExecution.getStepExecutions()).thenReturn(List.of(stepExecution));
        when(stepExecution.getReadCount()).thenReturn(100L);
        when(stepExecution.getWriteCount()).thenReturn(90L);
        when(stepExecution.getSkipCount()).thenReturn(10L);
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobExecution.getAllFailureExceptions()).thenReturn(
                List.of(new RuntimeException("Test error 1"), new RuntimeException("Test error 2")));

        // When/Then - no exception should be thrown
        importJobListener.afterJob(jobExecution);
    }
}
