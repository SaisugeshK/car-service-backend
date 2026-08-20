package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.*;

import java.util.List;

public interface PaymentTransactionService {

    PaymentTransactionResponseDTO create(PaymentTransactionRequestDTO dto);

    List<PaymentTransactionResponseDTO> getAll();

    PaymentTransactionResponseDTO getById(Long id);

    PaymentTransactionResponseDTO update(Long id, PaymentTransactionRequestDTO dto);

    void delete(Long id);
}