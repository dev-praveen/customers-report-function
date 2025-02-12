package com.spraveen.aws.service;

import com.spraveen.aws.exception.CsvGenerationException;
import com.spraveen.aws.model.Customer;
import com.spraveen.aws.repository.CustomerRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.io.StringWriter;

@Service
public class CustomerService {

  private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
  private static final String CSV_FILE_PREFIX = "customer_export";

  private final int batchSize;
  private final CustomerRepository customerRepository;
  private final S3Service s3Service;

  public CustomerService(
      @Value("${app.csv.batch-size}") int batchSize,
      CustomerRepository customerRepository,
      S3Service s3Service) {

    this.batchSize = batchSize;
    this.customerRepository = customerRepository;
    this.s3Service = s3Service;
  }

  @Transactional(readOnly = true)
  public String generateAndUploadCsvReport() {

    String csvContent = generateCsvContent();
    return s3Service.uploadCsvToS3(csvContent, CSV_FILE_PREFIX);
  }

  private String generateCsvContent() throws CsvGenerationException {

    StringWriter stringWriter = new StringWriter();

    try (CSVPrinter csvPrinter =
        new CSVPrinter(
            stringWriter,
            CSVFormat.DEFAULT
                .builder()
                .setHeader("id", "first_name", "last_name", "email", "contact_number")
                .build())) {

      // Get first page to determine total pages
      Page<Customer> firstPage =
          customerRepository.findCustomersInBatches(PageRequest.of(0, batchSize));
      long totalRecords = firstPage.getTotalElements();
      int totalPages = firstPage.getTotalPages();

      log.info(
          "Starting CSV export with batch size: {}, total records: {}, total pages: {}",
          batchSize,
          totalRecords,
          totalPages);

      int processedRecords = 0;

      // Process customers in batches
      for (int pageNumber = 0; pageNumber < totalPages; pageNumber++) {
        log.info("Processing page {} of {}", pageNumber + 1, totalPages);

        Page<Customer> page =
            pageNumber == 0
                ? firstPage
                : customerRepository.findCustomersInBatches(PageRequest.of(pageNumber, batchSize));

        for (Customer customer : page.getContent()) {
          csvPrinter.printRecord(
              customer.getId(),
              customer.getFirstName(),
              customer.getLastName(),
              customer.getEmail(),
              customer.getContactNumber());
          processedRecords++;
        }

        // Flush after each batch to avoid memory issues
        csvPrinter.flush();

        log.info("Processed {} records of {}", processedRecords, totalRecords);
      }

      return stringWriter.toString();
    } catch (IOException e) {
      log.error("Error generating CSV content", e);
      throw new CsvGenerationException("Failed to generate CSV content", e);
    }
  }
}
