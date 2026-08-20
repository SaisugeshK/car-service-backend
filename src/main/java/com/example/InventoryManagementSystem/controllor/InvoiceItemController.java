package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.InvoiceItemRequestDTO;
import com.example.InventoryManagementSystem.dto.InvoiceItemResponseDTO;
import com.example.InventoryManagementSystem.service.InvoiceItemService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoice-items")
@RequiredArgsConstructor
public class InvoiceItemController {

    private final InvoiceItemService service;

    @PostMapping
    public InvoiceItemResponseDTO createInvoiceItem(@RequestBody InvoiceItemRequestDTO dto) {
        return service.createInvoiceItem(dto);
    }

    @GetMapping
    public List<InvoiceItemResponseDTO> getAllInvoiceItems() {
        return service.getAllInvoiceItems();
    }

    @GetMapping("/{id}")
    public InvoiceItemResponseDTO getInvoiceItemById(@PathVariable Long id) {
        return service.getInvoiceItemById(id);
    }

    @PutMapping("/{id}")
    public InvoiceItemResponseDTO updateInvoiceItem(@PathVariable Long id, @RequestBody InvoiceItemRequestDTO dto) {
        return service.updateInvoiceItem(id, dto);
    }

    @DeleteMapping("/{id}")
    public String deleteInvoiceItem(@PathVariable Long id) {
        service.deleteInvoiceItem(id);
        return "Invoice Item deleted successfully";
    }
}
