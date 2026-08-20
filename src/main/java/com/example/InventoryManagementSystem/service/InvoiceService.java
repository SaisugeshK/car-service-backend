package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.InvoiceRequestDTO;
import com.example.InventoryManagementSystem.dto.InvoiceResponseDTO;

import java.util.List;

public interface InvoiceService {

    InvoiceResponseDTO createInvoice(InvoiceRequestDTO dto);

    List<InvoiceResponseDTO> getAllInvoices();

    InvoiceResponseDTO getInvoiceById(Long id);

    InvoiceResponseDTO updateInvoice(Long id, InvoiceRequestDTO dto);

    void deleteInvoice(Long id);

    InvoiceResponseDTO cancelInvoice(Long id);
}
