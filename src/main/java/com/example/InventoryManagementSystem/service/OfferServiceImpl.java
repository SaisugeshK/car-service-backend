package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.CategoryRepository;
import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.InvoiceRepository;
import com.example.InventoryManagementSystem.Repository.NotificationLogRepository;
import com.example.InventoryManagementSystem.Repository.OfferCampaignRepository;
import com.example.InventoryManagementSystem.Repository.OfferRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.dto.NotificationSendRequestDTO;
import com.example.InventoryManagementSystem.dto.OfferCampaignResponseDTO;
import com.example.InventoryManagementSystem.dto.OfferCheckRequestDTO;
import com.example.InventoryManagementSystem.dto.OfferEligibilityDTO;
import com.example.InventoryManagementSystem.dto.OfferLaunchRequestDTO;
import com.example.InventoryManagementSystem.dto.OfferRequestDTO;
import com.example.InventoryManagementSystem.dto.OfferResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Customer;
import com.example.InventoryManagementSystem.model.NotificationLog;
import com.example.InventoryManagementSystem.model.Offer;
import com.example.InventoryManagementSystem.model.OfferCampaign;
import com.example.InventoryManagementSystem.model.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final OfferCampaignRepository campaignRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationService notificationService;
    private final NotificationEventService notificationEventService;
    private final InvoiceRepository invoiceRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private static final Duration ENDING_SOON_WINDOW = Duration.ofHours(24);

    @Value("${app.timezone:Asia/Kolkata}")
    private String appTimezone;

    private ZoneId zone() {
        return ZoneId.of(appTimezone);
    }

    @Override
    @Transactional
    public OfferResponseDTO create(OfferRequestDTO dto) {
        Offer offer = new Offer();
        applyRequest(offer, dto);
        if (offer.getStatus() == null) offer.setStatus("ACTIVE");
        offer.setCouponCode(generateCouponCode(offer.getOfferName(), offer.getStartDateTime()));
        return mapToDto(offerRepository.save(offer));
    }

    @Override
    @Transactional
    public OfferResponseDTO update(Long id, OfferRequestDTO dto) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
        applyRequest(offer, dto);
        // The code stays as first generated even if the name changes — it may already be out
        // with customers. Only rows that predate coupon codes get one here.
        if (offer.getCouponCode() == null) {
            offer.setCouponCode(generateCouponCode(offer.getOfferName(), offer.getStartDateTime()));
        }
        return mapToDto(offerRepository.save(offer));
    }

    private void applyRequest(Offer offer, OfferRequestDTO dto) {
        if (dto.getOfferName() != null) offer.setOfferName(dto.getOfferName().trim());
        if (dto.getDescription() != null) offer.setDescription(dto.getDescription());
        if (dto.getDiscountType() != null) {
            String type = dto.getDiscountType().trim().toUpperCase();
            if (!"PERCENTAGE".equals(type) && !"FIXED_AMOUNT".equals(type)) {
                throw new IllegalArgumentException("discountType must be PERCENTAGE or FIXED_AMOUNT");
            }
            offer.setDiscountType(type);
        }
        if (dto.getDiscountValue() != null) offer.setDiscountValue(dto.getDiscountValue());
        if ("PERCENTAGE".equals(offer.getDiscountType()) && offer.getDiscountValue() != null
                && offer.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("A percentage discount cannot be more than 100%");
        }
        // Dates and usage limit are set as sent (null clears them) — the form always sends the
        // full offer, and "remove the end date" has to be expressible.
        offer.setStartDateTime(dto.getStartDateTime());
        offer.setEndDateTime(dto.getEndDateTime());
        if (offer.getStartDateTime() != null && offer.getEndDateTime() != null
                && !offer.getEndDateTime().isAfter(offer.getStartDateTime())) {
            throw new IllegalArgumentException("End date/time must be after the start date/time");
        }
        offer.setUsageLimit(dto.getUsageLimit());
        if (dto.getVehicleType() != null) offer.setVehicleType(dto.getVehicleType().isBlank() ? null : dto.getVehicleType());
        offer.setCategoryId(dto.getCategoryId());
        offer.setMinimumBillAmount(dto.getMinimumBillAmount());
        if (dto.getTerms() != null) offer.setTerms(dto.getTerms());
        if (dto.getStatus() != null) offer.setStatus(dto.getStatus());
    }

    // "Opening Offer" starting in 2026 -> OPENINGOFFER2026; a clash gets -2, -3, ...
    private String generateCouponCode(String offerName, OffsetDateTime start) {
        String base = offerName == null ? "" : offerName.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (base.isEmpty()) base = "OFFER";
        if (base.length() > 40) base = base.substring(0, 40);
        String year = String.valueOf((start != null ? start.atZoneSameInstant(zone()) : ZonedDateTime.now(zone())).getYear());
        if (!base.endsWith(year)) base = base + year;
        String code = base;
        int n = 2;
        while (offerRepository.existsByCouponCodeIgnoreCase(code)) {
            code = base + "-" + n++;
        }
        return code;
    }

    // Seeds coupon codes and start/end date-times on offers created before those existed.
    // Idempotent — once every row has both, this touches nothing.
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void backfillLegacyOffers() {
        for (Offer offer : offerRepository.findAll()) {
            boolean changed = false;
            if (offer.getStartDateTime() == null && offer.getLegacyStartDate() != null) {
                offer.setStartDateTime(offer.getLegacyStartDate().atStartOfDay(zone()).toOffsetDateTime());
                changed = true;
            }
            if (offer.getEndDateTime() == null && offer.getLegacyEndDate() != null) {
                offer.setEndDateTime(offer.getLegacyEndDate().atTime(LocalTime.of(23, 59, 59)).atZone(zone()).toOffsetDateTime());
                changed = true;
            }
            if (offer.getCouponCode() == null) {
                offer.setCouponCode(generateCouponCode(offer.getOfferName(), offer.getStartDateTime()));
                changed = true;
            }
            if (changed) offerRepository.save(offer);
        }
    }

    private long usedCount(Offer offer) {
        return invoiceRepository.countByOfferIdAndStatusNot(offer.getOfferId(), "CANCELLED");
    }

    // Upcoming/active/expired is worked out from the dates at the moment it's asked — no
    // scheduler needed, and it can never be stale.
    private String lifecycleStatus(Offer offer, long used, OffsetDateTime now) {
        if ("INACTIVE".equalsIgnoreCase(offer.getStatus())) return "INACTIVE";
        if (offer.getEndDateTime() != null && now.isAfter(offer.getEndDateTime())) return "EXPIRED";
        if (offer.getStartDateTime() != null && now.isBefore(offer.getStartDateTime())) return "UPCOMING";
        if (offer.getUsageLimit() != null && used >= offer.getUsageLimit()) return "LIMIT_REACHED";
        return "ACTIVE";
    }

    private String formatDateTime(OffsetDateTime t) {
        return t.atZoneSameInstant(zone()).format(DATE_TIME_FMT);
    }

    // Null = usable on this bill; otherwise the reason it isn't.
    private String ineligibleReason(Offer offer, long used, BigDecimal billAmount, String vehicleType, OffsetDateTime now) {
        switch (lifecycleStatus(offer, used, now)) {
            case "INACTIVE": return "This offer is currently disabled";
            case "UPCOMING": return "Offer not yet started — starts on " + formatDateTime(offer.getStartDateTime());
            case "EXPIRED": return "Offer expired on " + formatDateTime(offer.getEndDateTime());
            case "LIMIT_REACHED": return "Offer usage limit reached (" + offer.getUsageLimit() + " uses)";
            default: break;
        }
        if (offer.getVehicleType() != null && !offer.getVehicleType().isBlank()
                && (vehicleType == null || !offer.getVehicleType().equalsIgnoreCase(vehicleType))) {
            return "Offer is only for " + offer.getVehicleType() + " vehicles";
        }
        // categoryId is a label only — services carry no category, so it can't gate a bill.
        BigDecimal bill = billAmount != null ? billAmount : BigDecimal.ZERO;
        if (offer.getMinimumBillAmount() != null && bill.compareTo(offer.getMinimumBillAmount()) < 0) {
            return "Bill amount too low for this offer — minimum ₹" + offer.getMinimumBillAmount().setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }

    // Fixed: subtract directly. Percentage: bill × value / 100. Never more than the bill itself.
    private BigDecimal discountFor(Offer offer, BigDecimal billAmount) {
        BigDecimal bill = billAmount != null ? billAmount : BigDecimal.ZERO;
        BigDecimal raw = "PERCENTAGE".equals(offer.getDiscountType())
                ? bill.multiply(offer.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : offer.getDiscountValue().setScale(2, RoundingMode.HALF_UP);
        return raw.min(bill.setScale(2, RoundingMode.HALF_UP));
    }

    private OfferEligibilityDTO evaluate(Offer offer, OfferCheckRequestDTO bill, OffsetDateTime now) {
        long used = usedCount(offer);
        String reason = ineligibleReason(offer, used, bill.getBillAmount(), bill.getVehicleType(), now);
        return new OfferEligibilityDTO(mapToDto(offer, used, now), reason == null,
                reason == null ? "Offer applicable" : reason,
                reason == null ? discountFor(offer, bill.getBillAmount()) : BigDecimal.ZERO);
    }

    @Override
    public List<OfferEligibilityDTO> getApplicable(OfferCheckRequestDTO bill) {
        OffsetDateTime now = OffsetDateTime.now();
        return offerRepository.findAll().stream()
                .filter(o -> !"INACTIVE".equalsIgnoreCase(o.getStatus()))
                .filter(o -> o.getEndDateTime() == null || !now.isAfter(o.getEndDateTime()))
                .filter(o -> o.getVehicleType() == null || o.getVehicleType().isBlank() || bill.getVehicleType() == null
                        || o.getVehicleType().equalsIgnoreCase(bill.getVehicleType()))
                .map(o -> evaluate(o, bill, now))
                // Usable offers first, biggest discount first; upcoming/ineligible after.
                .sorted(Comparator.comparing(OfferEligibilityDTO::isEligible).reversed()
                        .thenComparing(OfferEligibilityDTO::getDiscountAmount, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    @Override
    public OfferEligibilityDTO checkCoupon(OfferCheckRequestDTO bill) {
        String code = bill.getCouponCode() != null ? bill.getCouponCode().trim() : "";
        if (code.isEmpty()) return new OfferEligibilityDTO(null, false, "Enter a coupon code", BigDecimal.ZERO);
        return offerRepository.findByCouponCodeIgnoreCase(code)
                .map(o -> evaluate(o, bill, OffsetDateTime.now()))
                .orElseGet(() -> new OfferEligibilityDTO(null, false, "Invalid coupon code", BigDecimal.ZERO));
    }

    @Override
    @Transactional
    public OfferRedemption redeem(String couponCode, BigDecimal billAmount, String vehicleType) {
        Offer found = offerRepository.findByCouponCodeIgnoreCase(couponCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon code: " + couponCode.trim()));
        Offer offer = offerRepository.findByIdForUpdate(found.getOfferId()).orElse(found);
        String reason = ineligibleReason(offer, usedCount(offer), billAmount, vehicleType, OffsetDateTime.now());
        if (reason != null) {
            throw new IllegalArgumentException("Coupon " + offer.getCouponCode() + ": " + reason);
        }
        return new OfferRedemption(offer, discountFor(offer, billAmount));
    }

    @Override
    public List<OfferResponseDTO> getAll() {
        return offerRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public OfferResponseDTO getById(Long id) {
        return mapToDto(offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id)));
    }

    @Override
    public void delete(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
        offerRepository.delete(offer);
    }

    @Override
    @Transactional
    public OfferCampaignResponseDTO launch(Long offerId, OfferLaunchRequestDTO dto) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));

        String channel = dto != null && dto.getChannel() != null && !dto.getChannel().isBlank()
                ? dto.getChannel().trim().toUpperCase() : "WHATSAPP";

        List<Customer> allCustomers = customerRepository.findAll();

        // Eligible = matches the offer's vehicle-type targeting. Null/blank vehicleType on the
        // offer means it applies to everyone, same "unset = both" convention used everywhere
        // else vehicleType appears (Product, ServiceMaster, Vehicle).
        List<Customer> eligibleCustomers;
        if (offer.getVehicleType() != null && !offer.getVehicleType().isBlank()) {
            Set<Long> matchingCustomerIds = vehicleRepository.findAll().stream()
                    .filter(v -> offer.getVehicleType().equals(v.getVehicleCategory()))
                    .map(Vehicle::getCustomerId)
                    .collect(Collectors.toSet());
            eligibleCustomers = allCustomers.stream()
                    .filter(c -> matchingCustomerIds.contains(c.getCustomerId()))
                    .collect(Collectors.toList());
        } else {
            eligibleCustomers = allCustomers;
        }

        OfferCampaign campaign = new OfferCampaign();
        campaign.setOfferId(offerId);
        campaign.setTotalCustomers(allCustomers.size());
        OfferCampaign savedCampaign = campaignRepository.save(campaign);

        String discountText = "PERCENTAGE".equals(offer.getDiscountType())
                ? offer.getDiscountValue() + "% off"
                : "₹" + offer.getDiscountValue() + " off";
        String validity = offer.getEndDateTime() != null
                ? " Valid until " + offer.getEndDateTime().atZoneSameInstant(zone()).format(DATE_FMT) + "." : "";
        String coupon = offer.getCouponCode() != null ? " Use code " + offer.getCouponCode() + "." : "";
        String message = offer.getOfferName() + " — " + discountText + "! " + (offer.getDescription() != null ? offer.getDescription() : "") + validity + coupon;

        for (Customer customer : eligibleCustomers) {
            String recipientPhone = "WHATSAPP".equals(channel)
                    ? (customer.getWhatsappNumber() != null ? customer.getWhatsappNumber() : customer.getPhone())
                    : customer.getPhone();

            NotificationSendRequestDTO sendDto = new NotificationSendRequestDTO();
            sendDto.setChannel(channel);
            sendDto.setRecipientPhone(recipientPhone);
            sendDto.setReferenceType("OFFER_CAMPAIGN");
            sendDto.setReferenceId(savedCampaign.getOfferCampaignId());
            sendDto.setSubject(offer.getOfferName());
            sendDto.setMessage(message);
            notificationService.send(sendDto);
        }

        OfferCampaignResponseDTO stats = buildStats(savedCampaign, eligibleCustomers.size());

        // Honest summary — sent/notConfigured/failed reflect NotificationLog exactly as recorded
        // above; never claims delivery this backend didn't actually confirm (same convention as
        // the log entries themselves).
        notificationEventService.raise("OFFER_CAMPAIGN_RESULT", "Offer campaign sent",
                offer.getOfferName() + ": " + stats.getSent() + " sent, " + stats.getNotConfigured()
                        + " not configured, " + stats.getFailed() + " failed (of " + eligibleCustomers.size() + " eligible).",
                "OFFER", offerId);

        return stats;
    }

    @Override
    public List<OfferCampaignResponseDTO> getCampaigns(Long offerId) {
        return campaignRepository.findByOfferIdOrderByLaunchedAtDesc(offerId).stream()
                .map(c -> buildStats(c, null))
                .collect(Collectors.toList());
    }

    /** eligibleOverride is passed right after launch (still in memory); otherwise recomputed from the log rows themselves. */
    private OfferCampaignResponseDTO buildStats(OfferCampaign campaign, Integer eligibleOverride) {
        List<NotificationLog> logs = notificationLogRepository
                .findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc("OFFER_CAMPAIGN", campaign.getOfferCampaignId());

        int sent = (int) logs.stream().filter(l -> "SENT".equals(l.getStatus())).count();
        int delivered = (int) logs.stream().filter(l -> "DELIVERED".equals(l.getStatus())).count();
        int failed = (int) logs.stream().filter(l -> "FAILED".equals(l.getStatus())).count();
        int notConfigured = (int) logs.stream().filter(l -> "NOT_CONFIGURED".equals(l.getStatus())).count();
        int eligible = eligibleOverride != null ? eligibleOverride : logs.size();
        int attempted = logs.size();

        OfferCampaignResponseDTO dto = new OfferCampaignResponseDTO();
        dto.setOfferCampaignId(campaign.getOfferCampaignId());
        dto.setOfferId(campaign.getOfferId());
        dto.setLaunchedAt(campaign.getLaunchedAt());
        dto.setTotalCustomers(campaign.getTotalCustomers());
        dto.setEligible(eligible);
        dto.setSent(sent);
        dto.setDelivered(delivered);
        dto.setFailed(failed);
        dto.setNotConfigured(notConfigured);
        dto.setPending(Math.max(0, eligible - attempted));
        return dto;
    }

    private OfferResponseDTO mapToDto(Offer offer) {
        return mapToDto(offer, usedCount(offer), OffsetDateTime.now());
    }

    private OfferResponseDTO mapToDto(Offer offer, long used, OffsetDateTime now) {
        OfferResponseDTO dto = new OfferResponseDTO();
        dto.setOfferId(offer.getOfferId());
        dto.setOfferName(offer.getOfferName());
        dto.setCouponCode(offer.getCouponCode());
        dto.setDescription(offer.getDescription());
        dto.setDiscountType(offer.getDiscountType());
        dto.setDiscountValue(offer.getDiscountValue());
        dto.setStartDateTime(offer.getStartDateTime());
        dto.setEndDateTime(offer.getEndDateTime());
        dto.setVehicleType(offer.getVehicleType());
        dto.setCategoryId(offer.getCategoryId());
        dto.setMinimumBillAmount(offer.getMinimumBillAmount());
        dto.setUsageLimit(offer.getUsageLimit());
        dto.setUsedCount(used);
        dto.setTerms(offer.getTerms());
        dto.setStatus(offer.getStatus());
        String lifecycle = lifecycleStatus(offer, used, now);
        dto.setLifecycleStatus(lifecycle);
        dto.setEndingSoon("ACTIVE".equals(lifecycle) && offer.getEndDateTime() != null
                && offer.getEndDateTime().isBefore(now.plus(ENDING_SOON_WINDOW)));
        dto.setCreatedAt(offer.getCreatedAt());
        if (offer.getCategoryId() != null) {
            categoryRepository.findById(offer.getCategoryId()).ifPresent(c -> dto.setCategoryName(c.getCategoryName()));
        }
        return dto;
    }
}
