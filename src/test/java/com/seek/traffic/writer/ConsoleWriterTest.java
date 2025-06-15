package com.seek.traffic.writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Console Writer Tests")
class ConsoleWriterTest {

	private ConsoleWriter consoleWriter;
	private ByteArrayOutputStream outputStream;
	private PrintStream originalOut;

	@BeforeEach
	void setUp() {
		outputStream = new ByteArrayOutputStream();
		originalOut = System.out;
		System.setOut(new PrintStream(outputStream));
		consoleWriter = new ConsoleWriter();
	}

	@Test
	@DisplayName("Should write single line to console")
	void shouldWriteSingleLineToConsole() {
		String testLine = "Test output line";

		consoleWriter.writeLine(testLine);

		String output = outputStream.toString();
		assertTrue(output.contains(testLine));
	}

	@Test
	@DisplayName("Should write multiple lines to console")
	void shouldWriteMultipleLinesToConsole() {
		String line1 = "First line";
		String line2 = "Second line";

		consoleWriter.writeLine(line1);
		consoleWriter.writeLine(line2);

		String output = outputStream.toString();
		assertTrue(output.contains(line1));
		assertTrue(output.contains(line2));
	}

	@Test
	@DisplayName("Should handle empty string")
	void shouldHandleEmptyString() {
		consoleWriter.writeLine("");

		String output = outputStream.toString();
		assertNotNull(output);
	}

	@Test
	@DisplayName("Should handle null input gracefully")
	void shouldHandleNullInputGracefully() {
		assertDoesNotThrow(() -> consoleWriter.writeLine(null));
	}

	@Test
	@DisplayName("Should write special characters correctly")
	void shouldWriteSpecialCharactersCorrectly() {
		String specialLine = "Special chars: @#$%^&*()";

		consoleWriter.writeLine(specialLine);

		String output = outputStream.toString();
		assertTrue(output.contains(specialLine));
	}

	void tearDown() {
		System.setOut(originalOut);
	}
}