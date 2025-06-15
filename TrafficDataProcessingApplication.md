# Traffic Data Processing Application

A Spring Boot batch application for processing and analyzing traffic data from CSV files.

## Overview

This application processes traffic data to provide insights such as:
- Traffic summaries by day
- Top half-hour periods with highest traffic
- Periods with least traffic
- Comprehensive traffic analytics

## Technology Stack

- **Java 17**
- **Spring Boot 3.5.0**
- **Spring Batch** - For batch processing
- **Spring MVC** - Web framework
- **Lombok** - Code generation
- **H2 Database** - In-memory database for batch processing
- **Maven** - Build tool

## Project Structure
```
src/
├── main/
│   ├── java/com/seek/traffic/
│   │   ├── batch/                # Batch processing components
│   │   │   ├── BatchConfig.java
│   │   │   ├── TrafficDataProcessor.java
│   │   │   ├── TrafficDataReader.java
│   │   │   └── TrafficDataWriter.java
│   │   ├── config/               # Configuration classes
│   │   │   └── InMemoryBatchConfig.java
│   │   ├── model/                # Data models
│   │   │   ├── DayTrafficSummary.java
│   │   │   ├── LeastTrafficPeriod.java
│   │   │   ├── TopHalfHour.java
│   │   │   └── TrafficData.java
│   │   ├── writer/               # Output writers
│   │   │   └── ConsoleWriter.java
│   │   └── TrafficApplication.java
│   └── resources/
│       ├── traffic_data.csv      # Sample traffic data
│       └── application.properties
└── test/                         # Unit tests
    └── java/com/seek/traffic/
        ├── batch/
        ├── model/
        └── writer/
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Getting Started

### 1. Clone the Repository
```
bash
git clone <repository-url>
cd traffic
```

### 2. Build the Application
```
bash
mvn clean compile
```

### 3. Run Tests
```
bash
mvn test
```

### 4. Run the Application
```
bash
mvn spring-boot:run
```

Or using the Maven wrapper:
```
bash
./mvnw spring-boot:run
```

### 5. Build JAR File
```
bash
mvn clean package
```

Run the JAR file:
```
bash
java -jar target/traffic-0.0.1-SNAPSHOT.jar
```

## Features

### Batch Processing
- Reads traffic data from CSV files
- Processes data using Spring Batch framework
- Generates various traffic analytics

### Data Models
- **TrafficData**: Raw traffic data model
- **DayTrafficSummary**: Daily traffic summary statistics
- **TopHalfHour**: Half-hour periods with highest traffic
- **LeastTrafficPeriod**: Periods with minimal traffic

### Output
- Console-based output for processed results
- Extensible writer framework for different output formats

## Configuration

The application uses `application.properties` for configuration. Key configurations include:

- Database settings (H2 in-memory)
- Batch job parameters
- File processing settings

## Data Format

The application expects CSV files with traffic data. Place your CSV files in the `src/main/resources/` directory.

## Testing

The application includes comprehensive unit tests for:
- Batch processing components
- Data models
- Writers
- Data readers and processors

Run tests with:
```
bash
mvn test
```

## Development

### Features

1. **Data Models**: Add to `com.seek.traffic.model` package
2. **Custom Processors**: Extend `com.seek.traffic.batch` package
3. **Output Writers**: Implement new writers in `com.seek.traffic.writer` package

### Code Style

The project uses Lombok for reducing boilerplate code. Make sure your IDE has Lombok plugin installed for proper code completion and compilation.

## Dependencies

Key dependencies include:

- `spring-boot-starter-batch` - Spring Batch support
- `spring-boot-starter-validation` - Validation framework
- `spring-boot-starter-web` - Web framework
- `lombok` - Code generation
- `commons-io` - File I/O utilities
- `h2` - In-memory database

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

[Add your license information here]

## Support

For questions or issues, please [create an issue](link-to-issues) or contact the development team.
