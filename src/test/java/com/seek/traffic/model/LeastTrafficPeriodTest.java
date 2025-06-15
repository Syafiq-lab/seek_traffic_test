
package com.seek.traffic.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Least Traffic Period Model Tests")
class LeastTrafficPeriodTest {

	@Test
	@DisplayName("Should create least traffic period with builder")
	void shouldCreateLeastTrafficPeriodWithBuilder() {
		LocalDateTime startTimestamp = LocalDateTime.of(2023, 12, 1, 15, 0);

		LeastTrafficPeriod period = LeastTrafficPeriod.builder()
				.startTimestamp(startTimestamp)
				.totalCars(20)
				.build();

		assertEquals(startTimestamp, period.getStartTimestamp());
		assertEquals(20, period.getTotalCars());
	}

	@Test
	@DisplayName("Should validate valid least traffic period")
	void shouldValidateValidLeastTrafficPeriod() {
		LeastTrafficPeriod period = LeastTrafficPeriod.builder()
				.startTimestamp(LocalDateTime.now())
				.totalCars(15)
				.build();

		assertTrue(period.isValid());
	}

	@Test
	@DisplayName("Should invalidate period with null start timestamp")
	void shouldInvalidatePeriodWithNullStartTimestamp() {
		LeastTrafficPeriod period = LeastTrafficPeriod.builder()
				.startTimestamp(null)
				.totalCars(15)
				.build();

		assertFalse(period.isValid());
	}

	@Test
	@DisplayName("Should invalidate period with negative total cars")
	void shouldInvalidatePeriodWithNegativeTotalCars() {
		LeastTrafficPeriod period = LeastTrafficPeriod.builder()
				.startTimestamp(LocalDateTime.now())
				.totalCars(-10)
				.build();

		assertFalse(period.isValid());
	}

	@Test
	@DisplayName("Should accept zero total cars as valid")
	void shouldAcceptZeroTotalCarsAsValid() {
		LeastTrafficPeriod period = LeastTrafficPeriod.builder()
				.startTimestamp(LocalDateTime.now())
				.totalCars(0)
				.build();

		assertTrue(period.isValid());
	}
}