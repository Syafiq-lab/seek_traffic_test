package com.seek.traffic.batch;

import com.seek.traffic.model.TrafficData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private static final String JOB_NAME = "trafficDataAnalysisJob";
    private static final String STEP_NAME = "trafficDataProcessingStep";
    private static final int CHUNK_SIZE = 100;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TrafficDataReader trafficDataReader;
    private final TrafficDataProcessor trafficDataProcessor;
    private final TrafficDataWriter trafficDataWriter;


    @Bean
    public Job trafficDataAnalysisJob() {
        log.info("Configuring traffic data analysis job: {}", JOB_NAME);

        return new JobBuilder(JOB_NAME, jobRepository)
                .start(trafficDataProcessingStep())
                .build();
    }


    @Bean
    public Step trafficDataProcessingStep() {
        log.info("Configuring traffic data processing step: {}", STEP_NAME);

        return new StepBuilder(STEP_NAME, jobRepository)
                .<TrafficData, Object>chunk(CHUNK_SIZE, transactionManager)
                .reader(trafficDataReader)
                .processor(trafficDataProcessor)
                .writer(trafficDataWriter)
                .faultTolerant()
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }
}