package com.seek.traffic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.CommandLineRunner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main Spring Boot application class for Traffic Data Analysis System.
 * 
 * This application processes traffic data using Spring Batch to provide:
 * - Daily traffic summaries
 * - Top half-hour traffic periods
 * - Least congested 1.5-hour periods
 */
@SpringBootApplication
@EnableBatchProcessing
public class TrafficApplication implements CommandLineRunner {
    
    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    private Job trafficDataAnalysisJob;  // This should match your job bean name from BatchConfig
    
    public static void main(String[] args) {
        SpringApplication.run(TrafficApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting traffic data analysis...");
        
        JobExecution execution = jobLauncher.run(
            trafficDataAnalysisJob, 
            new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters()
        );
        
        System.out.println("Job Status: " + execution.getStatus());
        System.out.println("Job completed successfully!");
    }
}