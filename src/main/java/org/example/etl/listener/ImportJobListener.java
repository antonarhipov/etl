package org.example.etl.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

/**
 * Listener that prints job execution summary after completion.
 */
@Component
public class ImportJobListener implements JobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(ImportJobListener.class);

    private final DuplicateSkipListener duplicateSkipListener;

    public ImportJobListener(DuplicateSkipListener duplicateSkipListener) {
        this.duplicateSkipListener = duplicateSkipListener;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        duplicateSkipListener.reset();
        logger.info("Starting temperature import job: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long totalRead = 0;
        long totalWritten = 0;
        long totalSkipped = 0;

        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            totalRead += stepExecution.getReadCount();
            totalWritten += stepExecution.getWriteCount();
            totalSkipped += stepExecution.getSkipCount();
        }

        int duplicatesSkipped = duplicateSkipListener.getSkipCount();

        BatchStatus status = jobExecution.getStatus();

        logger.info("===========================================");
        logger.info("       TEMPERATURE IMPORT JOB SUMMARY      ");
        logger.info("===========================================");
        logger.info("Status:                    {}", status);
        logger.info("Total records processed:   {}", totalRead);
        logger.info("Records inserted:          {}", totalWritten);
        logger.info("Duplicates skipped:        {}", duplicatesSkipped);
        logger.info("Total skipped:             {}", totalSkipped);

        if (jobExecution.getAllFailureExceptions().isEmpty()) {
            logger.info("Errors encountered:        0");
        } else {
            logger.info("Errors encountered:        {}", jobExecution.getAllFailureExceptions().size());
            for (Throwable exception : jobExecution.getAllFailureExceptions()) {
                logger.error("  - {}", exception.getMessage());
            }
        }
        logger.info("===========================================");
    }
}
