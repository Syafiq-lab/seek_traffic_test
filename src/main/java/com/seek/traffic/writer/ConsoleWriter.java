package com.seek.traffic.writer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
@Component
public class ConsoleWriter {
    
    private final PrintWriter writer;
    private final ReentrantLock writeLock = new ReentrantLock();
    
    public ConsoleWriter() {
        this.writer = new PrintWriter(System.out, true);
    }
    

    public void writeLine(String line) {
        writeLock.lock();
        try {
            writer.println(line);
            writer.flush();
            log.trace("Console output: {}", line);
        } catch (Exception e) {
            log.error("Failed to write line to console: {}", line, e);
            throw new ConsoleWriteException("Error writing to console", e);
        } finally {
            writeLock.unlock();
        }
    }
    

    public static class ConsoleWriteException extends RuntimeException {
        public ConsoleWriteException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}