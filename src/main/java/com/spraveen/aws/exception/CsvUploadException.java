package com.spraveen.aws.exception;

public class CsvUploadException extends RuntimeException {

  public CsvUploadException(String message) {
    super(message);
  }

  public CsvUploadException(String message, Throwable cause) {
    super(message, cause);
  }
}
