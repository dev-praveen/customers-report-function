package com.spraveen.aws.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.spraveen.aws.exception.CsvUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class S3Service {

  private static final Logger log = LoggerFactory.getLogger(S3Service.class);

  private final String bucketName;
  private final AmazonS3 amazonS3;

  public S3Service(AmazonS3 amazonS3, @Value("${aws.s3.bucket-name}") String bucketName) {
    this.amazonS3 = amazonS3;
    this.bucketName = bucketName;
  }

  /**
   * Uploads a CSV file to S3 with a timestamp-based filename
   *
   * @param csvContent The CSV content as a string
   * @param prefix Optional prefix for the file name (e.g., "customer_export")
   * @return The name of the uploaded file
   */
  public String uploadCsvToS3(String csvContent, String prefix) {
    try {
      // Generate filename with timestamp
      String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
      String fileName = String.format("%s_%s.csv", prefix, timestamp);

      // Prepare the content
      byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType("text/csv");
      metadata.setContentLength(csvBytes.length);

      // Upload to S3
      PutObjectRequest putObjectRequest =
          new PutObjectRequest(bucketName, fileName, new ByteArrayInputStream(csvBytes), metadata);
      amazonS3.putObject(putObjectRequest);

      log.info("CSV file uploaded to S3: {}/{}", bucketName, fileName);
      return fileName;
    } catch (Exception e) {
      log.error("Error uploading CSV to S3", e);
      throw new CsvUploadException("Failed to upload CSV to S3", e);
    }
  }
}
