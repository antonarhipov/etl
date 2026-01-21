package org.example.etl.config;

import org.example.etl.listener.DuplicateSkipListener;
import org.example.etl.listener.ImportJobListener;
import org.example.etl.model.TemperatureData;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Spring Batch configuration for temperature data import job.
 */
@Configuration
public class BatchConfiguration {

    private static final int CHUNK_SIZE = 100;
    private static final int SKIP_LIMIT = 10000;

    @Bean
    @StepScope
    public FlatFileItemReader<TemperatureData> temperatureReader(
            @Value("#{jobParameters['inputFile'] ?: '${etl.input.file}'}") Resource inputFile) {
        return new FlatFileItemReaderBuilder<TemperatureData>()
                .name("temperatureReader")
                .resource(inputFile)
                .linesToSkip(1) // Skip header row
                .delimited()
                .includedFields(0, 1, 2) // Only use first 3 columns: name, datetime, temp
                .names("name", "datetime", "temp")
                .fieldSetMapper(fieldSet -> new TemperatureData(
                        fieldSet.readString("name"),
                        LocalDateTime.parse(fieldSet.readString("datetime"), 
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        fieldSet.readDouble("temp")
                ))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<TemperatureData> temperatureWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<TemperatureData>()
                .sql("INSERT INTO temperature_data (name, datetime, temp) VALUES (:name, :datetime, :temp)")
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(item -> {
                    var params = new org.springframework.jdbc.core.namedparam.MapSqlParameterSource();
                    params.addValue("name", item.name());
                    params.addValue("datetime", item.datetime());
                    params.addValue("temp", item.temp());
                    return params;
                })
                .build();
    }

    @Bean
    public Step importStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           FlatFileItemReader<TemperatureData> reader,
                           JdbcBatchItemWriter<TemperatureData> writer,
                           DuplicateSkipListener skipListener) {
        return new StepBuilder("importStep", jobRepository)
                .<TemperatureData, TemperatureData>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .writer(writer)
                .faultTolerant()
                .skip(DataIntegrityViolationException.class)
                .skipLimit(SKIP_LIMIT)
                .listener(skipListener)
                .build();
    }

    @Bean
    public Job temperatureImportJob(JobRepository jobRepository,
                                    Step importStep,
                                    ImportJobListener jobListener) {
        return new JobBuilder("temperatureImportJob", jobRepository)
                .listener(jobListener)
                .start(importStep)
                .build();
    }
}
