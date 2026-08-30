package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.ServicePrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicePriceRepository extends JpaRepository<ServicePrice, Long> {

    List<ServicePrice> findByServiceId(Long serviceId);

    Optional<ServicePrice> findByServiceIdAndSizeClassCode(Long serviceId, String sizeClassCode);
}
