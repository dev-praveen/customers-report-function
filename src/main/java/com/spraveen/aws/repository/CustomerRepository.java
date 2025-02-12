package com.spraveen.aws.repository;

import com.spraveen.aws.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

  @Query("SELECT c FROM Customer c ORDER BY c.id")
  Page<Customer> findCustomersInBatches(Pageable pageable);
}
