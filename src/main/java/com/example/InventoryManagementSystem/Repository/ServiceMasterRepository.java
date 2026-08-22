package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.ServiceMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceMasterRepository extends JpaRepository<ServiceMaster, Long> {

    Optional<ServiceMaster> findByServiceName(String serviceName);
}
