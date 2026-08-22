package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.CategoryRepository;
import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.NotificationLogRepository;
import com.example.InventoryManagementSystem.Repository.OfferCampaignRepository;
import com.example.InventoryManagementSystem.Repository.OfferRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.dto.NotificationSendRequestDTO;
import com.example.InventoryManagementSystem.dto.OfferCampaignResponseDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Override
    public OfferResponseDTO create(OfferRequestDTO dto) {
        Offer offer = new Offer();
        applyRequest(offer, dto);
        if (offer.getStatus() == null) offer.setStatus("ACTIVE");
        return mapToDto(offerRepository.save(offer));
    }

    @Override
    public OfferResponseDTO update(Long id, OfferRequestDTO dto) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
        applyRequest(offer, dto);
        return mapToDto(offerRepository.save(offer));
    }

    private void applyRequest(Offer offer, OfferRequestDTO dto) {
        if (dto.getOfferName() != null) offer.setOfferName(dto.getOfferName());
        if (dto.getDescription() != null) offer.setDescription(dto.getDescription());
        if (dto.getDiscountType() != null) offer.setDiscountType(dto.getDiscountType());
        if (dto.getDiscountValue() != null) offer.setDiscountValue(dto.getDiscountValue());
        if (dto.getStartDate() != null) offer.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) offer.setEndDate(dto.getEndDate());
        if (dto.getVehicleType() != null) offer.setVehicleType(dto.getVehicleType());
        if (dto.getCategoryId() != null) offer.setCategoryId(dto.getCategoryId());
        if (dto.getMinimumBillAmount() != null) offer.setMinimumBillAmount(dto.getMinimumBillAmount());
        if (dto.getTerms() != null) offer.setTerms(dto.getTerms());
        if (dto.getStatus() != null) offer.setStatus(dto.getStatus());
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
        String validity = offer.getEndDate() != null ? " Valid until " + offer.getEndDate().format(DATE_FMT) + "." : "";
        String message = offer.getOfferName() + " — " + discountText + "! " + (offer.getDescription() != null ? offer.getDescription() : "") + validity;

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
        OfferResponseDTO dto = new OfferResponseDTO();
        dto.setOfferId(offer.getOfferId());
        dto.setOfferName(offer.getOfferName());
        dto.setDescription(offer.getDescription());
        dto.setDiscountType(offer.getDiscountType());
        dto.setDiscountValue(offer.getDiscountValue());
        dto.setStartDate(offer.getStartDate());
        dto.setEndDate(offer.getEndDate());
        dto.setVehicleType(offer.getVehicleType());
        dto.setCategoryId(offer.getCategoryId());
        dto.setMinimumBillAmount(offer.getMinimumBillAmount());
        dto.setTerms(offer.getTerms());
        dto.setStatus(offer.getStatus());
        dto.setCreatedAt(offer.getCreatedAt());
        if (offer.getCategoryId() != null) {
            categoryRepository.findById(offer.getCategoryId()).ifPresent(c -> dto.setCategoryName(c.getCategoryName()));
        }
        return dto;
    }
}
