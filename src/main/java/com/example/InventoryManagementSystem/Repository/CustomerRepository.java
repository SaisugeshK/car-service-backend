package com.example.InventoryManagementSystem.Repository;


import com.example.InventoryManagementSystem.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Mobile numbers are stored however they were typed ("+91 98765 43210", "9876543210", ...);
    // match on the last 10 digits only. :last10 must be exactly those 10 digits.
    @Query(value = "SELECT * FROM customers WHERE RIGHT(REGEXP_REPLACE(COALESCE(phone, ''), '[^0-9]', '', 'g'), 10) = :last10",
            nativeQuery = true)
    List<Customer> findByPhoneLast10(@Param("last10") String last10);
}
