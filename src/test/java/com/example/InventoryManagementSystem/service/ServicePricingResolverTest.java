package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.ServiceMasterRepository;
import com.example.InventoryManagementSystem.Repository.ServicePriceRepository;
import com.example.InventoryManagementSystem.model.ServiceMaster;
import com.example.InventoryManagementSystem.model.ServicePrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServicePricingResolverTest {

    private ServicePriceRepository servicePriceRepository;
    private ServiceMasterRepository serviceMasterRepository;
    private ServicePricingResolver resolver;

    @BeforeEach
    void setUp() {
        servicePriceRepository = mock(ServicePriceRepository.class);
        serviceMasterRepository = mock(ServiceMasterRepository.class);
        resolver = new ServicePricingResolver(servicePriceRepository, serviceMasterRepository);

        ServiceMaster svc = new ServiceMaster();
        svc.setServiceId(1L);
        svc.setDefaultPrice(new BigDecimal("999"));
        when(serviceMasterRepository.findById(1L)).thenReturn(Optional.of(svc));
        when(serviceMasterRepository.findById(eq(2L))).thenReturn(Optional.empty());

        when(servicePriceRepository.findByServiceIdAndSizeClassCode(any(), anyString())).thenReturn(Optional.empty());
        ServicePrice suv = new ServicePrice();
        suv.setServiceId(1L);
        suv.setSizeClassCode("SUV");
        suv.setPrice(new BigDecimal("3000"));
        when(servicePriceRepository.findByServiceIdAndSizeClassCode(1L, "SUV")).thenReturn(Optional.of(suv));
    }

    @Test
    void usesTheExplicitSizePrice() {
        assertEquals(new BigDecimal("3000"), resolver.priceFor(1L, "SUV"));
        assertEquals(new BigDecimal("3000"), resolver.priceFor(1L, " suv "));
    }

    @Test
    void fallsBackToDefaultPriceWhenSizeNotPriced() {
        assertEquals(new BigDecimal("999"), resolver.priceFor(1L, "SEDAN"));
        assertEquals(new BigDecimal("999"), resolver.priceFor(1L, null));
        assertEquals(new BigDecimal("999"), resolver.priceFor(1L, ""));
    }

    @Test
    void zeroWhenServiceMissing() {
        assertEquals(BigDecimal.ZERO, resolver.priceFor(2L, "SUV"));
    }
}
