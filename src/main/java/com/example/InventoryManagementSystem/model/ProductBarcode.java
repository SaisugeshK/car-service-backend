package com.example.InventoryManagementSystem.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_barcodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBarcode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long barcodeId;

    private Long productId;

    @Column(unique = true, nullable = false)
    private String barcode;
}