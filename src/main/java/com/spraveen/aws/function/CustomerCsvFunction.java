package com.spraveen.aws.function;

import com.spraveen.aws.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.function.UnaryOperator;

@RequiredArgsConstructor
@Component("customerCsvFunction")
public class CustomerCsvFunction implements UnaryOperator<String> {

  private final CustomerService customerService;

  @Override
  public String apply(String input) {
    return customerService.generateAndUploadCsvReport();
  }
}
