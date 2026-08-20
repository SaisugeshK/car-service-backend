package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.InvoiceItemRequestDTO;
import com.example.InventoryManagementSystem.dto.InvoiceItemResponseDTO;

import java.util.List;

public interface InvoiceItemService {

    InvoiceItemResponseDTO createInvoiceItem(InvoiceItemRequestDTO dto);

    List<InvoiceItemResponseDTO> getAllInvoiceItems();

    InvoiceItemResponseDTO getInvoiceItemById(Long id);

    InvoiceItemResponseDTO updateInvoiceItem(Long id, InvoiceItemRequestDTO dto);

    void deleteInvoiceItem(Long id);
}
