package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Offer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    Optional<Offer> findByCouponCodeIgnoreCase(String couponCode);

    boolean existsByCouponCodeIgnoreCase(String couponCode);

    // Row lock while an invoice redeems the offer, so two simultaneous invoices can't both pass
    // the usage-limit check and push it one over.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Offer o where o.offerId = :id")
    Optional<Offer> findByIdForUpdate(@Param("id") Long id);
}
