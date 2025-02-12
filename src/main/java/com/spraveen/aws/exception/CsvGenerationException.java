package com.spraveen.aws.exception;

public class CsvGenerationException extends RuntimeException {
    public CsvGenerationException(String message) {
        super(message);
    }

    public CsvGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
