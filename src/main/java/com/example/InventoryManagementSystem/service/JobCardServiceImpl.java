package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.AppointmentRepository;
import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.EstimateItemRepository;
import com.example.InventoryManagementSystem.Repository.EstimateRepository;
import com.example.InventoryManagementSystem.Repository.JobCardRepository;
import com.example.InventoryManagementSystem.Repository.ServiceReminderRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.dto.InvoiceLineItemRequestDTO;
import com.example.InventoryManagementSystem.dto.InvoiceRequestDTO;
import com.example.InventoryManagementSystem.dto.InvoiceResponseDTO;
import com.example.InventoryManagementSystem.dto.JobCardRequestDTO;
import com.example.InventoryManagementSystem.dto.JobCardResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Appointment;
import com.example.InventoryManagementSystem.model.Customer;
import com.example.InventoryManagementSystem.model.Estimate;
import com.example.InventoryManagementSystem.model.EstimateItem;
import com.example.InventoryManagementSystem.model.JobCard;
import com.example.InventoryManagementSystem.model.ServiceReminder;
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
    private final ServiceReminderRepository serviceReminderRepository;
    private final InvoiceService invoiceService;

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
        saved.setJobCardNumber("JC-" + saved.getJobCardId());
        saved = jobCardRepository.save(saved);

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
        applyRequest(jobCard, dto);
        if (dto.getStatus() != null) jobCard.setStatus(dto.getStatus());
        return mapToDto(jobCardRepository.save(jobCard));
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
            "WAITING_FOR_PARTS", "QUALITY_CHECK", "READY_FOR_DELIVERY", "DELIVERED", "CANCELLED");

    @Override
    public JobCardResponseDTO updateStatus(Long id, String status) {
        if (status == null || !VALID_STATUSES.contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Invalid job card status: " + status);
        }
        JobCard jobCard = jobCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job card not found with id: " + id));
        jobCard.setStatus(status.toUpperCase());
        return mapToDto(jobCardRepository.save(jobCard));
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
        saved.setJobCardNumber("JC-" + saved.getJobCardId());
        saved = jobCardRepository.save(saved);

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

        InvoiceRequestDTO invoiceRequest = new InvoiceRequestDTO();
        invoiceRequest.setCustomerId(jobCard.getCustomerId());
        invoiceRequest.setVehicleId(jobCard.getVehicleId());
        invoiceRequest.setOdometerReading(jobCard.getOdometer());
        invoiceRequest.setCounterId(counterId);
        invoiceRequest.setPaymentMethod(paymentMethod);
        invoiceRequest.setPaidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO);
        invoiceRequest.setDiscountAmount(estimate.getDiscountAmount());
        invoiceRequest.setItems(lines);

        InvoiceResponseDTO invoice = invoiceService.createInvoice(invoiceRequest);

        jobCard.setInvoiceId(invoice.getInvoiceId());
        jobCard.setEstimateId(estimate.getEstimateId());
        if (!"READY_FOR_DELIVERY".equals(jobCard.getStatus())) {
            jobCard.setStatus("READY_FOR_DELIVERY");
        }
        jobCardRepository.save(jobCard);

        return invoice;
    }

    @Override
    @Transactional
    public JobCardResponseDTO markDelivered(Long jobCardId) {
        JobCard jobCard = jobCardRepository.findById(jobCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Job card not found with id: " + jobCardId));
        if (jobCard.getInvoiceId() == null) {
            throw new IllegalArgumentException("Cannot deliver a job card with no invoice");
        }

        jobCard.setStatus("DELIVERED");
        jobCard.setDeliveredAt(OffsetDateTime.now());
        jobCardRepository.save(jobCard);

        Vehicle vehicle = vehicleRepository.findById(jobCard.getVehicleId()).orElse(null);
        if (vehicle != null && jobCard.getOdometer() != null) {
            vehicle.setOdometer(jobCard.getOdometer());
            vehicleRepository.save(vehicle);
        }

        InvoiceResponseDTO invoice = invoiceService.getInvoiceById(jobCard.getInvoiceId());
        boolean hasService = invoice.getItems() != null
                && invoice.getItems().stream().anyMatch(i -> "SERVICE".equals(i.getItemType()));
        if (hasService) {
            ServiceReminder reminder = new ServiceReminder();
            reminder.setVehicleId(jobCard.getVehicleId());
            reminder.setDueDate(LocalDate.now(ZoneOffset.UTC).plusMonths(6));
            reminder.setDueOdometer(jobCard.getOdometer() != null ? jobCard.getOdometer() + 5000 : null);
            reminder.setSourceInvoiceId(jobCard.getInvoiceId());
            reminder.setStatus("UPCOMING");
            reminder.setNotes("Auto-created on delivery of " + jobCard.getJobCardNumber());
            serviceReminderRepository.save(reminder);
        }

        return mapToDto(jobCard);
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
        dto.setCreatedAt(jobCard.getCreatedAt());
        dto.setUpdatedAt(jobCard.getUpdatedAt());

        Customer customer = customerRepository.findById(jobCard.getCustomerId()).orElse(null);
        if (customer != null) {
            dto.setCustomerName(customer.getCustomerName());
            dto.setCustomerPhone(customer.getPhone());
        }
        Vehicle vehicle = vehicleRepository.findById(jobCard.getVehicleId()).orElse(null);
        if (vehicle != null) {
            dto.setVehicleModel(vehicle.getVehicleModel());
            dto.setRegistrationNumber(vehicle.getRegistrationNumber());
        }
        if (jobCard.getAdvisorUserId() != null) {
            userRepository.findById(jobCard.getAdvisorUserId()).ifPresent(u -> dto.setAdvisorName(displayName(u)));
        }
        if (jobCard.getTechnicianUserId() != null) {
            userRepository.findById(jobCard.getTechnicianUserId()).ifPresent(u -> dto.setTechnicianName(displayName(u)));
        }

        return dto;
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getUsername();
    }
}
