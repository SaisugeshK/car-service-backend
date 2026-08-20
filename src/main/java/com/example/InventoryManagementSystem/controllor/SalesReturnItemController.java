package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.SalesReturnItemRequestDTO;
import com.example.InventoryManagementSystem.dto.SalesReturnItemResponseDTO;
import com.example.InventoryManagementSystem.service.SalesReturnItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/sales-return-items")
@RequiredArgsConstructor
public class SalesReturnItemController {

    private final SalesReturnItemService salesReturnItemService;

    // CREATE ITEM
    @PostMapping
    public ResponseEntity<SalesReturnItemResponseDTO> create(
            @RequestBody SalesReturnItemRequestDTO dto) {
        return ResponseEntity.ok(salesReturnItemService.createItem(dto));
    }

    // GET ALL ✔ NEW
    @GetMapping
    public ResponseEntity<List<SalesReturnItemResponseDTO>> getAll() {
        return ResponseEntity.ok(salesReturnItemService.getAll());
    }

    // GET BY RETURN ID
    @GetMapping("/{salesReturnId}")
    public ResponseEntity<List<SalesReturnItemResponseDTO>> getByReturnId(
            @PathVariable Long salesReturnId) {
        return ResponseEntity.ok(
                salesReturnItemService.getByReturnId(salesReturnId)
        );
    }

    // UPDATE ITEM (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<SalesReturnItemResponseDTO> update(
            @PathVariable Long id,
            @RequestBody SalesReturnItemRequestDTO dto) {

        return ResponseEntity.ok(
                salesReturnItemService.updateItem(id, dto)
        );
    }
    // DELETE ITEM
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        salesReturnItemService.deleteItem(id);
        return ResponseEntity.ok("Sales return item deleted and stock updated");
    }
}