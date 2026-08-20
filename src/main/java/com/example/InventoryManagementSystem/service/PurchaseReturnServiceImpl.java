package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.PurchaseRepository;
import com.example.InventoryManagementSystem.Repository.SupplierRepository;
import com.example.InventoryManagementSystem.dto.PurchaseReturnRequestDTO;
import com.example.InventoryManagementSystem.dto.PurchaseReturnResponseDTO;
import com.example.InventoryManagementSystem.model.Purchase;
import com.example.InventoryManagementSystem.model.PurchaseReturn;
import com.example.InventoryManagementSystem.Repository.PurchaseReturnRepository;
import com.example.InventoryManagementSystem.model.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseReturnServiceImpl
        implements PurchaseReturnService {

    private final PurchaseReturnRepository purchaseReturnRepository;

    private final PurchaseRepository purchaseRepository;

    private final SupplierRepository supplierRepository;


    @Override
    public PurchaseReturnResponseDTO createPurchaseReturn(
            PurchaseReturnRequestDTO requestDTO) {

        Purchase purchase = purchaseRepository.findById(requestDTO.getPurchaseId())
                .orElseThrow(() -> new RuntimeException("Purchase not found"));

        Supplier supplier = supplierRepository.findById(requestDTO.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        PurchaseReturn entity = PurchaseReturn.builder()
                .purchaseId(Math.toIntExact(purchase.getPurchaseId()))
                .supplierId(Math.toIntExact(supplier.getSupplierId()))
                .returnDate(LocalDateTime.now())
                .totalAmount(requestDTO.getTotalAmount())
                .notes(requestDTO.getNotes())
                .build();

        PurchaseReturn saved = purchaseReturnRepository.save(entity);

        return mapToResponse(saved);
    }

    @Override
    public PurchaseReturnResponseDTO getPurchaseReturnById(Integer id) {

        PurchaseReturn entity = purchaseReturnRepository.findById(id).orElse(null);

        if (entity == null) {
            return null;
        }

        return mapToResponse(entity);
    }

    @Override
    public List<PurchaseReturnResponseDTO> getAllPurchaseReturns() {

        return purchaseReturnRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PurchaseReturnResponseDTO updatePurchaseReturn(
            Integer id,
            PurchaseReturnRequestDTO requestDTO) {

        PurchaseReturn entity =purchaseReturnRepository.findById(id).orElse(null);

        if (entity == null) {
            return null;
        }

        entity.setPurchaseId(Math.toIntExact(requestDTO.getPurchaseId()));
        entity.setSupplierId(Math.toIntExact(requestDTO.getSupplierId()));
        entity.setTotalAmount(requestDTO.getTotalAmount());
        entity.setNotes(requestDTO.getNotes());

        PurchaseReturn updated = purchaseReturnRepository.save(entity);

        return mapToResponse(updated);
    }

    @Override
    public void deletePurchaseReturn(Integer id) {

        PurchaseReturn entity = purchaseReturnRepository.findById(id).orElse(null);

        if (entity != null) {
            purchaseReturnRepository.delete(entity);
        }
    }

    private PurchaseReturnResponseDTO mapToResponse(
            PurchaseReturn entity) {

        return PurchaseReturnResponseDTO.builder()
                .purchaseReturnId(entity.getPurchaseReturnId())
                .purchaseId(entity.getPurchaseId())
                .supplierId(entity.getSupplierId())
                .returnDate(entity.getReturnDate())
                .totalAmount(entity.getTotalAmount())
                .notes(entity.getNotes())
                .build();
    }
}