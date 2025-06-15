package com.seek.traffic.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Day Traffic Summary Model Tests")
class DayTrafficSummaryTest {

	@Test
	@DisplayName("Should create day traffic summary with builder")
	void shouldCreateDayTrafficSummaryWithBuilder() {
		LocalDate date = LocalDate.of(2023, 12, 1);

		DayTrafficSummary summary = DayTrafficSummary.builder()
				.date(date)
				.totalCars(150)
				.build();

		assertEquals(date, summary.getDate());
		assertEquals(150, summary.getTotalCars());
	}

	@Test
	@DisplayName("Should validate valid day traffic summary")
	void shouldValidateValidDayTrafficSummary() {
		DayTrafficSummary summary = DayTrafficSummary.builder()
				.date(LocalDate.now())
				.totalCars(100)
				.build();

		assertTrue(summary.isValid());
	}

	@Test
	@DisplayName("Should invalidate summary with null date")
	void shouldInvalidateSummaryWithNullDate() {
		DayTrafficSummary summary = DayTrafficSummary.builder()
				.date(null)
				.totalCars(100)
				.build();

		assertFalse(summary.isValid());
	}

	@Test
	@DisplayName("Should invalidate summary with negative total cars")
	void shouldInvalidateSummaryWithNegativeTotalCars() {
		DayTrafficSummary summary = DayTrafficSummary.builder()
				.date(LocalDate.now())
				.totalCars(-10)
				.build();

		assertFalse(summary.isValid());
	}

	@Test
	@DisplayName("Should accept zero total cars as valid")
	void shouldAcceptZeroTotalCarsAsValid() {
		DayTrafficSummary summary = DayTrafficSummary.builder()
				.date(LocalDate.now())
				.totalCars(0)
				.build();

		assertTrue(summary.isValid());
	}
}