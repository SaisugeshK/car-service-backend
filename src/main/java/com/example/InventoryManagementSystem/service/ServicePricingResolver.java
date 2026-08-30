package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.ServiceMasterRepository;
import com.example.InventoryManagementSystem.Repository.ServicePriceRepository;
import com.example.InventoryManagementSystem.model.ServiceMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The one place the price of a service for a given vehicle size is decided: an explicit
 * service_prices row for that size, else the service's defaultPrice, else zero. Used by the
 * Estimate and Additional Work builders when no unitPrice override was supplied.
 */
@Service
@RequiredArgsConstructor
public class ServicePricingResolver {

    private final ServicePriceRepository servicePriceRepository;
    private final ServiceMasterRepository serviceMasterRepository;

    public BigDecimal priceFor(Long serviceId, String sizeClassCode) {
        if (sizeClassCode != null && !sizeClassCode.isBlank()) {
            var sp = servicePriceRepository.findByServiceIdAndSizeClassCode(serviceId, sizeClassCode.trim().toUpperCase());
            if (sp.isPresent() && sp.get().getPrice() != null) {
                return sp.get().getPrice();
            }
        }
        return serviceMasterRepository.findById(serviceId)
                .map(ServiceMaster::getDefaultPrice)
                .filter(Objects::nonNull)
                .orElse(BigDecimal.ZERO);
    }
}
