package com.seek.traffic.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Top Half Hour Model Tests")
class TopHalfHourTest {

	@Test
	@DisplayName("Should create top half hour with builder")
	void shouldCreateTopHalfHourWithBuilder() {
		LocalDateTime timestamp = LocalDateTime.of(2023, 12, 1, 10, 30);

		TopHalfHour topHalfHour = TopHalfHour.builder()
				.timestamp(timestamp)
				.carsCount(45)
				.build();

		assertEquals(timestamp, topHalfHour.getTimestamp());
		assertEquals(45, topHalfHour.getCarsCount());
	}

	@Test
	@DisplayName("Should validate valid top half hour")
	void shouldValidateValidTopHalfHour() {
		TopHalfHour topHalfHour = TopHalfHour.builder()
				.timestamp(LocalDateTime.now())
				.carsCount(30)
				.build();

		assertTrue(topHalfHour.isValid());
	}

	@Test
	@DisplayName("Should invalidate top half hour with null timestamp")
	void shouldInvalidateTopHalfHourWithNullTimestamp() {
		TopHalfHour topHalfHour = TopHalfHour.builder()
				.timestamp(null)
				.carsCount(30)
				.build();

		assertFalse(topHalfHour.isValid());
	}

	@Test
	@DisplayName("Should invalidate top half hour with negative car count")
	void shouldInvalidateTopHalfHourWithNegativeCarCount() {
		TopHalfHour topHalfHour = TopHalfHour.builder()
				.timestamp(LocalDateTime.now())
				.carsCount(-5)
				.build();

		assertFalse(topHalfHour.isValid());
	}

	@Test
	@DisplayName("Should accept zero car count as valid")
	void shouldAcceptZeroCarCountAsValid() {
		TopHalfHour topHalfHour = TopHalfHour.builder()
				.timestamp(LocalDateTime.now())
				.carsCount(0)
				.build();

		assertTrue(topHalfHour.isValid());
	}
}