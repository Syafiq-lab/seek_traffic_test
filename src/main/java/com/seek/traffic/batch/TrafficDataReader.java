package com.seek.traffic.batch;

import com.seek.traffic.model.TrafficData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;



@Slf4j
@Component
public class TrafficDataReader extends FlatFileItemReader<TrafficData> {

    private static final String CSV_FILE_PATH = "traffic_data.csv";
    private static final String[] COLUMN_NAMES = {"timestamp", "cars_count"}; // Changed to match CSV header
    private static final DateTimeFormatter[] SUPPORTED_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"), // Added ISO format with 'T'
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")
    };

    public TrafficDataReader() {
        super();
        initializeReader();
        log.info("TrafficDataReader initialized for file: {}", CSV_FILE_PATH);
    }

    private void initializeReader() {
        setName("trafficDataReader");
        setResource(new ClassPathResource(CSV_FILE_PATH));
        setLinesToSkip(1); // Skip header row
        setLineMapper(createLineMapper());
        setStrict(true); // Fail on parsing errors
    }

    private LineMapper<TrafficData> createLineMapper() {
        DefaultLineMapper<TrafficData> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames(COLUMN_NAMES);
        tokenizer.setStrict(true);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new TrafficDataFieldSetMapper());

        return lineMapper;
    }

    private static class TrafficDataFieldSetMapper implements FieldSetMapper<TrafficData> {

        @Override
        public TrafficData mapFieldSet(FieldSet fieldSet) throws BindException {
            try {
                String timestampStr = fieldSet.readString("timestamp");
                int carsCount = fieldSet.readInt("cars_count"); // Changed to match CSV header

                LocalDateTime timestamp = parseTimestamp(timestampStr);

                if (carsCount < 0) {
                    throw new IllegalArgumentException("Cars count cannot be negative: " + carsCount);
                }

                TrafficData trafficData = TrafficData.builder()
                        .timestamp(timestamp)
                        .carsCount(carsCount)
                        .build();

                log.trace("Mapped traffic data: {} cars at {}", carsCount, timestamp);
                return trafficData;

            } catch (Exception e) {
                log.error("Error mapping field set to TrafficData: {}", fieldSet.getValues(), e);
                throw new TrafficDataMappingException("Failed to map CSV row to TrafficData", e);
            }
        }

        private LocalDateTime parseTimestamp(String timestampStr) throws DateTimeParseException {
            if (timestampStr == null || timestampStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Timestamp cannot be null or empty");
            }

            timestampStr = timestampStr.trim();

            for (DateTimeFormatter formatter : SUPPORTED_FORMATTERS) {
                try {
                    return LocalDateTime.parse(timestampStr, formatter);
                } catch (DateTimeParseException e) {
                    log.trace("Failed to parse timestamp '{}' with format '{}'",
                            timestampStr, formatter.toString());
                }
            }

            throw new DateTimeParseException(
                    "Unable to parse timestamp: " + timestampStr +
                            ". Supported formats: yyyy-MM-dd'T'HH:mm:ss, yyyy-MM-dd HH:mm:ss, yyyy-MM-dd HH:mm, MM/dd/yyyy HH:mm:ss, MM/dd/yyyy HH:mm",
                    timestampStr, 0);
        }
    }

    public static class TrafficDataMappingException extends RuntimeException {
        public TrafficDataMappingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}