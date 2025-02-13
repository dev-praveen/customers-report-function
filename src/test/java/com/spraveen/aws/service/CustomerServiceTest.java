package com.spraveen.aws.service;

import com.spraveen.aws.model.Customer;
import com.spraveen.aws.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

  @Mock CustomerRepository customerRepository;
  @Mock S3Service s3Service;
  CustomerService customerService;

  @BeforeEach
  void setUp() {
    customerService = new CustomerService(1, customerRepository, s3Service);
  }

  @Test
  void generateAndUploadCsvReport() {

    when(s3Service.uploadCsvToS3(any(String.class), any(String.class)))
        .thenReturn("dummy-file-name");

    Page<Customer> page = new PageImpl<>(getCustomers());
    when(customerRepository.findCustomersInBatches(any(Pageable.class))).thenReturn(page);

    final String csvReport = customerService.generateAndUploadCsvReport();

    assertThat(csvReport).isEqualTo("dummy-file-name");
  }

  List<Customer> getCustomers() {

    Customer customer1 = new Customer();
    customer1.setId(1L);
    customer1.setFirstName("John");
    customer1.setLastName("Doe");
    customer1.setEmail("john.doe@example.com");
    customer1.setContactNumber("1234567890");

    Customer customer2 = new Customer();
    customer2.setId(2L);
    customer2.setFirstName("Jane");
    customer2.setLastName("Doe");
    customer2.setEmail("jane.doe@example.com");
    customer2.setContactNumber("0987654321");

    return List.of(customer1, customer2);
  }
}
