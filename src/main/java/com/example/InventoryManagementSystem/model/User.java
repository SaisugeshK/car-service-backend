package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;

    private String firstName;

    private String lastName;

    private String fullName;

    @Column(unique = true)
    private String email;

    private String mobileNumber;

    private String passwordHash;

    private Integer roleId;

    private String status;

    private Boolean active;
}