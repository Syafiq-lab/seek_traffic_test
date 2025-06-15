
package com.seek.traffic.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Traffic Data Model Tests")
class TrafficDataTest {

	@Test
	@DisplayName("Should create traffic data with builder")
	void shouldCreateTrafficDataWithBuilder() {
		LocalDateTime timestamp = LocalDateTime.of(2023, 12, 1, 10, 30);

		TrafficData trafficData = TrafficData.builder()
				.timestamp(timestamp)
				.carsCount(25)
				.build();

		assertEquals(timestamp, trafficData.getTimestamp());
		assertEquals(25, trafficData.getCarsCount());
	}

	@Test
	@DisplayName("Should validate valid traffic data")
	void shouldValidateValidTrafficData() {
		TrafficData trafficData = TrafficData.builder()
				.timestamp(LocalDateTime.now())
				.carsCount(10)
				.build();

		assertTrue(trafficData.isValid());
	}

	@Test
	@DisplayName("Should invalidate traffic data with null timestamp")
	void shouldInvalidateTrafficDataWithNullTimestamp() {
		TrafficData trafficData = TrafficData.builder()
				.timestamp(null)
				.carsCount(10)
				.build();

		assertFalse(trafficData.isValid());
	}

	@Test
	@DisplayName("Should invalidate traffic data with negative car count")
	void shouldInvalidateTrafficDataWithNegativeCarCount() {
		TrafficData trafficData = TrafficData.builder()
				.timestamp(LocalDateTime.now())
				.carsCount(-5)
				.build();

		assertFalse(trafficData.isValid());
	}

	@Test
	@DisplayName("Should accept zero car count as valid")
	void shouldAcceptZeroCarCountAsValid() {
		TrafficData trafficData = TrafficData.builder()
				.timestamp(LocalDateTime.now())
				.carsCount(0)
				.build();

		assertTrue(trafficData.isValid());
	}

	@Test
	@DisplayName("Should implement equals and hashCode correctly")
	void shouldImplementEqualsAndHashCodeCorrectly() {
		LocalDateTime timestamp = LocalDateTime.of(2023, 12, 1, 10, 30);

		TrafficData data1 = TrafficData.builder()
				.timestamp(timestamp)
				.carsCount(25)
				.build();

		TrafficData data2 = TrafficData.builder()
				.timestamp(timestamp)
				.carsCount(25)
				.build();

		assertEquals(data1, data2);
		assertEquals(data1.hashCode(), data2.hashCode());
	}
}