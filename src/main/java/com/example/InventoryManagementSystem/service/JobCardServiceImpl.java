package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.AdditionalWorkItemRepository;
import com.example.InventoryManagementSystem.Repository.AdditionalWorkRequestRepository;
import com.example.InventoryManagementSystem.Repository.AppointmentRepository;
import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.EstimateItemRepository;
import com.example.InventoryManagementSystem.Repository.EstimateRepository;
import com.example.InventoryManagementSystem.Repository.JobCardRepository;
import com.example.InventoryManagementSystem.Repository.JobCardStatusHistoryRepository;
import com.example.InventoryManagementSystem.Repository.ServiceMasterRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.dto.DeliveryChecklistDTO;
import com.example.InventoryManagementSystem.dto.InvoiceLineItemRequestDTO;
import com.example.InventoryManagementSystem.dto.InvoiceRequestDTO;
import com.example.InventoryManagementSystem.dto.InvoiceResponseDTO;
import com.example.InventoryManagementSystem.dto.JobCardRequestDTO;
import com.example.InventoryManagementSystem.dto.JobCardResponseDTO;
import com.example.InventoryManagementSystem.dto.JobCardStatusHistoryResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.AdditionalWorkItem;
import com.example.InventoryManagementSystem.model.AdditionalWorkRequest;
import com.example.InventoryManagementSystem.model.Appointment;
import com.example.InventoryManagementSystem.model.Customer;
import com.example.InventoryManagementSystem.model.Estimate;
import com.example.InventoryManagementSystem.model.EstimateItem;
import com.example.InventoryManagementSystem.model.JobCard;
import com.example.InventoryManagementSystem.model.JobCardStatusHistory;
import com.example.InventoryManagementSystem.model.ServiceMaster;
import com.example.InventoryManagementSystem.model.User;
import com.example.InventoryManagementSystem.model.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobCardServiceImpl implements JobCardService {

    private final JobCardRepository jobCardRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final EstimateRepository estimateRepository;
    private final EstimateItemRepository estimateItemRepository;
    private final ServiceReminderService serviceReminderService;
    private final InvoiceService invoiceService;
    private final ServiceMasterRepository serviceMasterRepository;
    private final JobCardStatusHistoryRepository statusHistoryRepository;
    private final AdditionalWorkRequestRepository additionalWorkRequestRepository;
    private final AdditionalWorkItemRepository additionalWorkItemRepository;
    private final NotificationEventService notificationEventService;
    private final AuditLogService auditLogService;
    private final SettingsLookupService settingsLookupService;

    // Writes one JobCardStatusHistory row — called by every place that actually changes a job
    // card's status, right after the new status is set, so the visual timeline can't miss a
    // transition. Callers are responsible for only calling this when the status truly changed
    // (each call site already only reaches this after comparing old vs new).
    private void recordStatusChange(Long jobCardId, String status) {
        JobCardStatusHistory h = new JobCardStatusHistory();
        h.setJobCardId(jobCardId);
        h.setStatus(status);
        statusHistoryRepository.save(h);

        // Every path that moves a job card to READY_FOR_DELIVERY goes through here — one trigger
        // point instead of duplicating this at createJobCard/updateJobCard/updateStatus.
        if ("READY_FOR_DELIVERY".equals(status)) {
            jobCardRepository.findById(jobCardId).ifPresent(jc ->
                    notificationEventService.raise("READY_FOR_DELIVERY", "Vehicle ready for delivery",
                            "Job card " + jc.getJobCardNumber() + " is ready for delivery.", "JOB_CARD", jobCardId));
        }

        // Phase 30 — same choke point covers "Job status changed" for every transition (including
        // the very first RECEIVED at creation, which is itself an auditable fact) and calls out
        // "Delivery completed" as its own action, matching the spec's separate line item for it.
        boolean isDelivery = "DELIVERED".equals(status);
        auditLogService.record(isDelivery ? "DELIVERY_COMPLETED" : "JOB_STATUS_CHANGED", "JOB_CARD", jobCardId,
                "Job card #" + jobCardId + (isDelivery ? " delivered." : " status changed to " + status + "."));
    }

    // Auto-created the first time it's needed — a real catalog Service (shows up in Services,
    // taxed like any other line item) rather than a special-cased invoice line type.
    private static final String INSPECTION_FEE_SERVICE_NAME = "Inspection Fee";

    @Override
    public JobCardResponseDTO createJobCard(JobCardRequestDTO dto) {
        customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + dto.getVehicleId()));
        if (!vehicle.getCustomerId().equals(dto.getCustomerId())) {
            throw new IllegalArgumentException("Selected vehicle does not belong to this customer");
        }

        JobCard jobCard = new JobCard();
        applyRequest(jobCard, dto);
        jobCard.setStatus(dto.getStatus() != null ? dto.getStatus() : "RECEIVED");

        JobCard saved = jobCardRepository.save(jobCard);
        saved.setJobCardNumber(settingsLookupService.get("job_card_prefix", "JC") + "-" + saved.getJobCardId());
        saved = jobCardRepository.save(saved);
        recordStatusChange(saved.getJobCardId(), saved.getStatus());

        notificationEventService.raise("NEW_JOB", "New job card",
                "Job card " + saved.getJobCardNumber() + " created for " + vehicle.getVehicleModel()
                        + " (" + vehicle.getRegistrationNumber() + ").", "JOB_CARD", saved.getJobCardId());

        return mapToDto(saved);
    }

    @Override
    public JobCardResponseDTO getJobCardById(Long id) {
        return mapToDto(jobCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job card not found with id: " + id)));
    }

    @Override
    public List<JobCardResponseDTO> getAllJobCards() {
        return jobCardRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public JobCardResponseDTO updateJobCard(Long id, JobCardRequestDTO dto) {
        JobCard jobCard = jobCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job card not found with id: " + id));
        // Billing safety: DELIVERED can only be reached through markDelivered()/POST .../deliver,
        // which enforces the delivery checklist. A generic field update must never be able to
        // fast-forward a job card past that gate.
        if (dto.getStatus() != null && "DELIVERED".equalsIgnoreCase(dto.getStatus())
                && !"DELIVERED".equals(jobCard.getStatus())) {
            throw new IllegalArgumentException("Cannot set status to DELIVERED directly — use the delivery checklist endpoint");
        }
        applyRequest(jobCard, dto);
        boolean statusChanging = dto.getStatus() != null && !dto.getStatus().equals(jobCard.getStatus());
        if (dto.getStatus() != null) jobCard.setStatus(dto.getStatus());
        JobCard saved = jobCardRepository.save(jobCard);
        if (statusChanging) recordStatusChange(saved.getJobCardId(), saved.getStatus());
        return mapToDto(saved);
    }

    private void applyRequest(JobCard jobCard, JobCardRequestDTO dto) {
        if (dto.getCustomerId() != null) jobCard.setCustomerId(dto.getCustomerId());
        if (dto.getVehicleId() != null) jobCard.setVehicleId(dto.getVehicleId());
        if (dto.getAdvisorUserId() != null) jobCard.setAdvisorUserId(dto.getAdvisorUserId());
        if (dto.getTechnicianUserId() != null) jobCard.setTechnicianUserId(dto.getTechnicianUserId());
        if (dto.getAppointmentId() != null) jobCard.setAppointmentId(dto.getAppointmentId());
        if (dto.getExpectedDelivery() != null) jobCard.setExpectedDelivery(dto.getExpectedDelivery());
        if (dto.getOdometer() != null) jobCard.setOdometer(dto.getOdometer());
        if (dto.getFuelLevel() != null) jobCard.setFuelLevel(dto.getFuelLevel());
        if (dto.getComplaint() != null) jobCard.setComplaint(dto.getComplaint());
        if (dto.getKeysReceived() != null) jobCard.setKeysReceived(dto.getKeysReceived());
        if (dto.getAccessoriesReceived() != null) jobCard.setAccessoriesReceived(dto.getAccessoriesReceived());
        if (dto.getWorkRequired() != null) jobCard.setWorkRequired(dto.getWorkRequired());
        if (dto.getInternalNotes() != null) jobCard.setInternalNotes(dto.getInternalNotes());
        if (dto.getVehicleConditionNotes() != null) jobCard.setVehicleConditionNotes(dto.getVehicleConditionNotes());
    }

    private static final List<String> VALID_STATUSES = List.of(
            "RECEIVED", "INSPECTION", "ESTIMATE", "WAITING_APPROVAL", "APPROVED", "IN_PROGRESS",
            "WAITING_FOR_PARTS", "ADDITIONAL_APPROVAL_REQUIRED", "QUALITY_CHECK", "READY_FOR_DELIVERY",
            "DELIVERED", "CANCELLED");

    @Override
    public JobCardResponseDTO updateStatus(Long id, String status) {
        if (status == null || !VALID_STATUSES.contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Invalid job card status: " + status);
        }
        JobCard jobCard = jobCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job card not found with id: " + id));
        // Billing safety: same DELIVERED gate as updateJobCard() — only markDelivered() may set it.
        if ("DELIVERED".equalsIgnoreCase(status) && !"DELIVERED".equals(jobCard.getStatus())) {
            throw new IllegalArgumentException("Cannot set status to DELIVERED directly — use the delivery checklist endpoint");
        }
        boolean statusChanging = !status.toUpperCase().equals(jobCard.getStatus());
        jobCard.setStatus(status.toUpperCase());
        JobCard saved = jobCardRepository.save(jobCard);
        if (statusChanging) recordStatusChange(saved.getJobCardId(), saved.getStatus());
        return mapToDto(saved);
    }

    @Override
    public void deleteJobCard(Long id) {
        JobCard jobCard = jobCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job card not found with id: " + id));
        jobCardRepository.delete(jobCard);
    }

    @Override
    @Transactional
    public JobCardResponseDTO createFromAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
        if (appointment.getJobCardId() != null) {
            throw new IllegalArgumentException("This appointment has already been converted to a job card");
        }
        if (appointment.getVehicleId() == null) {
            throw new IllegalArgumentException("Appointment has no vehicle to convert into a job card");
        }

        JobCard jobCard = new JobCard();
        jobCard.setCustomerId(appointment.getCustomerId());
        jobCard.setVehicleId(appointment.getVehicleId());
        jobCard.setAdvisorUserId(appointment.getAdvisorUserId());
        jobCard.setAppointmentId(appointmentId);
        jobCard.setComplaint(appointment.getRequestedService());
        jobCard.setStatus("RECEIVED");

        JobCard saved = jobCardRepository.save(jobCard);
        saved.setJobCardNumber(settingsLookupService.get("job_card_prefix", "JC") + "-" + saved.getJobCardId());
        saved = jobCardRepository.save(saved);
        recordStatusChange(saved.getJobCardId(), saved.getStatus());

        appointment.setJobCardId(saved.getJobCardId());
        appointment.setStatus("ARRIVED");
        appointmentRepository.save(appointment);

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public InvoiceResponseDTO generateInvoice(Long jobCardId, String paymentMethod, BigDecimal paidAmount, Long counterId) {
        JobCard jobCard = jobCardRepository.findById(jobCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Job card not found with id: " + jobCardId));
        if (jobCard.getInvoiceId() != null) {
            throw new IllegalArgumentException("This job card already has an invoice");
        }

        Estimate estimate = estimateRepository.findByJobCardId(jobCardId).stream()
                .filter(e -> "APPROVED".equals(e.getStatus()))
                .max(Comparator.comparing(Estimate::getCreatedAt))
                .orElseThrow(() -> new IllegalArgumentException("No approved estimate found for this job card"));

        List<EstimateItem> estimateItems = estimateItemRepository.findByEstimateId(estimate.getEstimateId());
        if (estimateItems.isEmpty()) {
            throw new IllegalArgumentException("Approved estimate has no line items");
        }

        List<InvoiceLineItemRequestDTO> lines = new ArrayList<>();
        for (EstimateItem item : estimateItems) {
            InvoiceLineItemRequestDTO line = new InvoiceLineItemRequestDTO();
            line.setItemType(item.getItemType());
            line.setProductId(item.getProductId());
            line.setServiceId(item.getServiceId());
            line.setDescription(item.getDescription());
            line.setQuantity(item.getQuantity());
            line.setUnitPrice(item.getUnitPrice());
            line.setDiscount(item.getDiscount());
            lines.add(line);
        }

        // Fold in every APPROVED additional-work item discovered mid-service — REJECTED/PENDING
        // ones never reach the invoice, per spec ("do not bill it").
        List<Long> approvedAdditionalWorkIds = additionalWorkRequestRepository.findByJobCardIdOrderByRequestedAtDesc(jobCardId).stream()
                .filter(r -> "APPROVED".equals(r.getStatus()))
                .map(AdditionalWorkRequest::getAdditionalWorkRequestId)
                .collect(Collectors.toList());
        if (!approvedAdditionalWorkIds.isEmpty()) {
            for (AdditionalWorkItem item : additionalWorkItemRepository.findByAdditionalWorkRequestIdIn(approvedAdditionalWorkIds)) {
                InvoiceLineItemRequestDTO line = new InvoiceLineItemRequestDTO();
                line.setItemType(item.getItemType());
                line.setProductId(item.getProductId());
                line.setServiceId(item.getServiceId());
                line.setDescription(item.getDescription());
                line.setQuantity(item.getQuantity());
                line.setUnitPrice(item.getUnitPrice());
                line.setDiscount(item.getDiscount());
                lines.add(line);
            }
        }

        InvoiceRequestDTO invoiceRequest = new InvoiceRequestDTO();
        invoiceRequest.setCustomerId(jobCard.getCustomerId());
        invoiceRequest.setVehicleId(jobCard.getVehicleId());
        invoiceRequest.setOdometerReading(jobCard.getOdometer());
        invoiceRequest.setCounterId(counterId);
        invoiceRequest.setPaymentMethod(paymentMethod);
        invoiceRequest.setPaidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO);
        invoiceRequest.setDiscountAmount(estimate.getDiscountAmount());
        invoiceRequest.setItems(lines);
        invoiceRequest.setJobCardId(jobCardId);

        InvoiceResponseDTO invoice = invoiceService.createInvoice(invoiceRequest);

        jobCard.setInvoiceId(invoice.getInvoiceId());
        jobCard.setEstimateId(estimate.getEstimateId());
        boolean statusChanging = !"READY_FOR_DELIVERY".equals(jobCard.getStatus());
        if (statusChanging) jobCard.setStatus("READY_FOR_DELIVERY");
        JobCard saved = jobCardRepository.save(jobCard);
        if (statusChanging) recordStatusChange(saved.getJobCardId(), saved.getStatus());

        return invoice;
    }

    @Override
    @Transactional
    public InvoiceResponseDTO generateInspectionFeeInvoice(Long jobCardId, String paymentMethod, BigDecimal paidAmount, Long counterId, BigDecimal feeAmount) {
        JobCard jobCard = jobCardRepository.findById(jobCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Job card not found with id: " + jobCardId));
        if (jobCard.getInvoiceId() != null) {
            throw new IllegalArgumentException("This job card already has an invoice");
        }

        List<Estimate> estimates = estimateRepository.findByJobCardId(jobCardId);
        // Billing safety: never available while there's still a chargeable path — an approved
        // estimate means the real invoice should be generated instead, and a still-pending or
        // changes-requested estimate means the customer hasn't actually decided yet.
        if (estimates.stream().anyMatch(e -> "APPROVED".equals(e.getStatus()))) {
            throw new IllegalArgumentException("This job card has an approved estimate — generate the full invoice instead");
        }
        if (estimates.stream().anyMatch(e -> "PENDING".equals(e.getStatus()) || "CHANGES_REQUESTED".equals(e.getStatus()))) {
            throw new IllegalArgumentException("This job card has an estimate still awaiting the customer's decision");
        }
        if (estimates.stream().noneMatch(e -> "REJECTED".equals(e.getStatus()))) {
            throw new IllegalArgumentException("No rejected estimate found for this job card");
        }

        ServiceMaster inspectionFeeService = serviceMasterRepository.findByServiceName(INSPECTION_FEE_SERVICE_NAME)
                .orElseGet(() -> {
                    ServiceMaster s = new ServiceMaster();
                    s.setServiceName(INSPECTION_FEE_SERVICE_NAME);
                    s.setDescription("Charged when a customer declines the estimated work after inspection.");
                    s.setDefaultPrice(BigDecimal.valueOf(500));
                    s.setGstPercentage(BigDecimal.valueOf(18));
                    s.setStatus("active");
                    return serviceMasterRepository.save(s);
                });

        BigDecimal amount = feeAmount != null ? feeAmount : inspectionFeeService.getDefaultPrice();

        InvoiceLineItemRequestDTO line = new InvoiceLineItemRequestDTO();
        line.setItemType("SERVICE");
        line.setServiceId(inspectionFeeService.getServiceId());
        line.setDescription(INSPECTION_FEE_SERVICE_NAME);
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPrice(amount);
        line.setDiscount(BigDecimal.ZERO);

        InvoiceRequestDTO invoiceRequest = new InvoiceRequestDTO();
        invoiceRequest.setCustomerId(jobCard.getCustomerId());
        invoiceRequest.setVehicleId(jobCard.getVehicleId());
        invoiceRequest.setOdometerReading(jobCard.getOdometer());
        invoiceRequest.setCounterId(counterId);
        invoiceRequest.setPaymentMethod(paymentMethod);
        invoiceRequest.setPaidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO);
        invoiceRequest.setDiscountAmount(BigDecimal.ZERO);
        invoiceRequest.setItems(List.of(line));
        invoiceRequest.setJobCardId(jobCardId);

        InvoiceResponseDTO invoice = invoiceService.createInvoice(invoiceRequest);

        jobCard.setInvoiceId(invoice.getInvoiceId());
        boolean statusChanging = !"READY_FOR_DELIVERY".equals(jobCard.getStatus());
        if (statusChanging) jobCard.setStatus("READY_FOR_DELIVERY");
        JobCard savedCard = jobCardRepository.save(jobCard);
        if (statusChanging) recordStatusChange(savedCard.getJobCardId(), savedCard.getStatus());

        return invoice;
    }

    @Override
    @Transactional
    public JobCardResponseDTO markDelivered(Long jobCardId, DeliveryChecklistDTO checklist) {
        JobCard jobCard = jobCardRepository.findById(jobCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Job card not found with id: " + jobCardId));
        if (jobCard.getInvoiceId() == null) {
            throw new IllegalArgumentException("Cannot deliver a job card with no invoice");
        }
        // Pre-deployment fix — DELIVERED is a terminal state everywhere else in this app treats
        // terminal states (an approved Estimate can't be re-approved, a PAID SalaryPayment can't
        // be re-marked paid), but this endpoint had no such guard: calling it again on an
        // already-delivered job card silently overwrote deliveredAt/deliveredByUserId with no
        // trace of the original delivery, discovered during the pre-deployment negative-test pass.
        if ("DELIVERED".equals(jobCard.getStatus())) {
            throw new IllegalArgumentException("This job card has already been delivered");
        }

        // The checklist is the final authority, not just a frontend gate — a direct API call
        // can't skip it either.
        if (checklist == null || checklist.getDeliveredByUserId() == null) {
            throw new IllegalArgumentException("Delivery checklist: select which staff member is delivering the vehicle");
        }
        if (!Boolean.TRUE.equals(checklist.getVehicleCleaned())) {
            throw new IllegalArgumentException("Delivery checklist: vehicle cleaning must be confirmed");
        }
        if (!Boolean.TRUE.equals(checklist.getBelongingsChecked())) {
            throw new IllegalArgumentException("Delivery checklist: customer belongings must be confirmed checked");
        }
        if (!Boolean.TRUE.equals(checklist.getKeysReady())) {
            throw new IllegalArgumentException("Delivery checklist: keys must be confirmed ready");
        }

        boolean statusChanging = !"DELIVERED".equals(jobCard.getStatus());
        jobCard.setStatus("DELIVERED");
        jobCard.setDeliveredAt(OffsetDateTime.now());
        jobCard.setDeliveredByUserId(checklist.getDeliveredByUserId());
        jobCard.setVehicleCleaned(true);
        jobCard.setBelongingsChecked(true);
        jobCard.setKeysReadyForDelivery(true);
        jobCardRepository.save(jobCard);
        if (statusChanging) recordStatusChange(jobCard.getJobCardId(), "DELIVERED");

        Vehicle vehicle = vehicleRepository.findById(jobCard.getVehicleId()).orElse(null);
        if (vehicle != null && jobCard.getOdometer() != null) {
            vehicle.setOdometer(jobCard.getOdometer());
            vehicleRepository.save(vehicle);
        }

        InvoiceResponseDTO invoice = invoiceService.getInvoiceById(jobCard.getInvoiceId());
        boolean hasService = invoice.getItems() != null
                && invoice.getItems().stream().anyMatch(i -> "SERVICE".equals(i.getItemType()));
        if (hasService) {
            Integer odo = jobCard.getOdometer();
            serviceReminderService.upsertAutoReminder(jobCard.getVehicleId(), "NEXT_SERVICE",
                    LocalDate.now(ZoneOffset.UTC).plusMonths(6), odo != null ? odo + 5000 : null,
                    "Auto-created on delivery of " + jobCard.getJobCardNumber());
            serviceReminderService.upsertAutoReminder(jobCard.getVehicleId(), "OIL_CHANGE",
                    LocalDate.now(ZoneOffset.UTC).plusMonths(3), odo != null ? odo + 3000 : null,
                    "Auto-created on delivery of " + jobCard.getJobCardNumber());
        }

        return mapToDto(jobCard);
    }

    @Override
    public List<JobCardStatusHistoryResponseDTO> getStatusHistory(Long jobCardId) {
        return statusHistoryRepository.findByJobCardIdOrderByChangedAtAsc(jobCardId).stream()
                .map(h -> {
                    JobCardStatusHistoryResponseDTO dto = new JobCardStatusHistoryResponseDTO();
                    dto.setStatus(h.getStatus());
                    dto.setChangedAt(h.getChangedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private JobCardResponseDTO mapToDto(JobCard jobCard) {
        JobCardResponseDTO dto = new JobCardResponseDTO();
        dto.setJobCardId(jobCard.getJobCardId());
        dto.setJobCardNumber(jobCard.getJobCardNumber());
        dto.setCustomerId(jobCard.getCustomerId());
        dto.setVehicleId(jobCard.getVehicleId());
        dto.setAdvisorUserId(jobCard.getAdvisorUserId());
        dto.setTechnicianUserId(jobCard.getTechnicianUserId());
        dto.setAppointmentId(jobCard.getAppointmentId());
        dto.setEstimateId(jobCard.getEstimateId());
        dto.setInvoiceId(jobCard.getInvoiceId());
        dto.setDateIn(jobCard.getDateIn());
        dto.setExpectedDelivery(jobCard.getExpectedDelivery());
        dto.setOdometer(jobCard.getOdometer());
        dto.setFuelLevel(jobCard.getFuelLevel());
        dto.setComplaint(jobCard.getComplaint());
        dto.setKeysReceived(jobCard.getKeysReceived());
        dto.setAccessoriesReceived(jobCard.getAccessoriesReceived());
        dto.setWorkRequired(jobCard.getWorkRequired());
        dto.setInternalNotes(jobCard.getInternalNotes());
        dto.setVehicleConditionNotes(jobCard.getVehicleConditionNotes());
        dto.setStatus(jobCard.getStatus());
        dto.setDeliveredAt(jobCard.getDeliveredAt());
        dto.setDeliveredByUserId(jobCard.getDeliveredByUserId());
        dto.setVehicleCleaned(jobCard.getVehicleCleaned());
        dto.setBelongingsChecked(jobCard.getBelongingsChecked());
        dto.setKeysReadyForDelivery(jobCard.getKeysReadyForDelivery());
        dto.setCreatedAt(jobCard.getCreatedAt());
        dto.setUpdatedAt(jobCard.getUpdatedAt());

        Customer customer = customerRepository.findById(jobCard.getCustomerId()).orElse(null);
        if (customer != null) {
            dto.setCustomerName(customer.getCustomerName());
            dto.setCustomerPhone(customer.getPhone());
            dto.setCustomerWhatsapp(customer.getWhatsappNumber());
        }
        Vehicle vehicle = vehicleRepository.findById(jobCard.getVehicleId()).orElse(null);
        if (vehicle != null) {
            dto.setVehicleModel(vehicle.getVehicleModel());
            dto.setRegistrationNumber(vehicle.getRegistrationNumber());
            dto.setVehicleCategory(vehicle.getVehicleCategory());
            dto.setVehicleSizeClass(vehicle.getSizeClass());
        }
        if (jobCard.getAdvisorUserId() != null) {
            userRepository.findById(jobCard.getAdvisorUserId()).ifPresent(u -> dto.setAdvisorName(displayName(u)));
        }
        if (jobCard.getTechnicianUserId() != null) {
            userRepository.findById(jobCard.getTechnicianUserId()).ifPresent(u -> dto.setTechnicianName(displayName(u)));
        }
        if (jobCard.getDeliveredByUserId() != null) {
            userRepository.findById(jobCard.getDeliveredByUserId()).ifPresent(u -> dto.setDeliveredByName(displayName(u)));
        }
        if (jobCard.getInvoiceId() != null) {
            try {
                dto.setInvoiceNumber(invoiceService.getInvoiceById(jobCard.getInvoiceId()).getInvoiceNumber());
            } catch (Exception ignored) {
                // Invoice lookup is best-effort here — a stale invoiceId should never break
                // loading the job card itself.
            }
        }

        return dto;
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getUsername();
    }
}
