package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.JobCardRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.Repository.VisitRepository;
import com.example.InventoryManagementSystem.dto.VisitLookupDTO;
import com.example.InventoryManagementSystem.dto.VisitRequestDTO;
import com.example.InventoryManagementSystem.dto.VisitResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Customer;
import com.example.InventoryManagementSystem.model.JobCard;
import com.example.InventoryManagementSystem.model.User;
import com.example.InventoryManagementSystem.model.Vehicle;
import com.example.InventoryManagementSystem.model.Visit;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

// Visits identify a customer by mobile number + vehicle registration number:
//   registration known        -> linked to that vehicle and its owner
//   only mobile known         -> linked to that customer; the new registration is added as their vehicle
//   neither known             -> a new customer and vehicle are created
// Every change recomputes the customer's visit count and New/Occasional/Regular status.
//
// Visibility: every logged-in role may log visits and read a customer's visit history (employees
// can already read customers). The full visit list is SUPER_ADMIN's; an EMPLOYEE's list is the
// visits they handled.
@Service
@RequiredArgsConstructor
public class VisitService {

    public static final Set<String> PURPOSES = Set.of("SERVICE", "REPAIR", "INQUIRY", "INSURANCE_RENEWAL", "OTHER");

    private final VisitRepository visitRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final JobCardRepository jobCardRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final CustomerVisitStatsService statsService;
    private final AuditLogService auditLogService;

    public static String normalizeRegistration(String reg) {
        return reg == null ? "" : reg.toUpperCase().replaceAll("[\\s-]", "");
    }

    static String last10Digits(String phone) {
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        return digits.length() > 10 ? digits.substring(digits.length() - 10) : digits;
    }

    private Vehicle findVehicle(String registration) {
        String normalized = normalizeRegistration(registration);
        if (normalized.isEmpty()) return null;
        return vehicleRepository.findByNormalizedRegistration(normalized).stream().findFirst().orElse(null);
    }

    private Customer findCustomerByPhone(String phone) {
        String last10 = last10Digits(phone);
        if (last10.length() != 10) return null;
        return customerRepository.findByPhoneLast10(last10).stream().findFirst().orElse(null);
    }

    public VisitLookupDTO lookup(String phone, String registration) {
        VisitLookupDTO dto = new VisitLookupDTO();
        Vehicle vehicle = findVehicle(registration);
        Customer byPhone = findCustomerByPhone(phone);
        Customer owner = vehicle != null ? customerRepository.findById(vehicle.getCustomerId()).orElse(null) : null;
        Customer customer = owner != null ? owner : byPhone;
        if (customer != null) {
            dto.setCustomerId(customer.getCustomerId());
            dto.setCustomerName(customer.getCustomerName());
            dto.setCustomerPhone(customer.getPhone());
            dto.setRegularStatus(customer.getRegularStatus());
            dto.setTotalVisits(customer.getTotalVisits());
            dto.setLastVisitDate(customer.getLastVisitDate());
        }
        if (vehicle != null) {
            dto.setVehicleId(vehicle.getVehicleId());
            dto.setVehicleModel(vehicle.getVehicleModel());
            dto.setRegistrationNumber(vehicle.getRegistrationNumber());
        }
        if (owner != null && byPhone != null && !owner.getCustomerId().equals(byPhone.getCustomerId())) {
            dto.setOwnerMismatch(true);
            dto.setVehicleOwnerName(owner.getCustomerName());
        }
        return dto;
    }

    @Transactional
    public VisitResponseDTO log(VisitRequestDTO req) {
        String purpose = req.getPurpose().trim().toUpperCase();
        if (!PURPOSES.contains(purpose)) {
            throw new IllegalArgumentException("Purpose must be one of " + PURPOSES);
        }

        boolean createdCustomer = false;
        boolean createdVehicle = false;
        Customer customer = null;
        Vehicle vehicle = null;

        // 1) The vehicle is the strongest identity — a plate belongs to exactly one owner.
        if (req.getVehicleId() != null) {
            vehicle = vehicleRepository.findById(req.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + req.getVehicleId()));
        } else if (req.getRegistrationNumber() != null && !req.getRegistrationNumber().isBlank()) {
            vehicle = findVehicle(req.getRegistrationNumber());
        }
        if (vehicle != null) {
            customer = customerRepository.findById(vehicle.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("The vehicle's customer no longer exists"));
        }

        // 2) Otherwise the customer by id or mobile number.
        if (customer == null && req.getCustomerId() != null) {
            customer = customerRepository.findById(req.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + req.getCustomerId()));
        }
        if (customer == null) {
            customer = findCustomerByPhone(req.getPhone());
        }

        // 3) Neither known — a new customer.
        if (customer == null) {
            if (last10Digits(req.getPhone()).length() != 10) {
                throw new IllegalArgumentException("Enter a valid 10-digit mobile number");
            }
            if (req.getCustomerName() == null || req.getCustomerName().isBlank()) {
                throw new IllegalArgumentException("New customer — enter the customer's name");
            }
            Customer c = new Customer();
            c.setCustomerName(req.getCustomerName().trim());
            c.setPhone(req.getPhone().trim());
            c.setStatus("active");
            customer = customerRepository.save(c);
            createdCustomer = true;
        }

        // 4) A registration number we haven't seen — add it as this customer's vehicle.
        if (vehicle == null && req.getRegistrationNumber() != null && !req.getRegistrationNumber().isBlank()) {
            if (req.getVehicleModel() == null || req.getVehicleModel().isBlank()) {
                throw new IllegalArgumentException("New vehicle — enter the vehicle model");
            }
            Vehicle v = new Vehicle();
            v.setCustomerId(customer.getCustomerId());
            v.setRegistrationNumber(req.getRegistrationNumber().trim().toUpperCase());
            v.setVehicleModel(req.getVehicleModel().trim());
            if (req.getVehicleCategory() != null && !req.getVehicleCategory().isBlank()) {
                v.setVehicleCategory(req.getVehicleCategory().trim().toUpperCase());
            }
            vehicle = vehicleRepository.save(v);
            createdVehicle = true;
        }
        if (vehicle == null) {
            throw new IllegalArgumentException("Enter the vehicle registration number");
        }

        User me = currentUserService.getCurrentUser();
        Visit visit = new Visit();
        visit.setCustomerId(customer.getCustomerId());
        visit.setVehicleId(vehicle.getVehicleId());
        visit.setVisitDateTime(req.getVisitDateTime() != null ? req.getVisitDateTime() : OffsetDateTime.now());
        visit.setPurpose(purpose);
        visit.setNotes(req.getNotes());
        visit.setHandledByUserId(me != null ? me.getUserId() : null);
        visit.setHandledByName(currentUserService.getCurrentUsername());
        visit.setSource("MANUAL");
        Visit saved = visitRepository.save(visit);

        statsService.recompute(customer.getCustomerId());
        auditLogService.record("VISIT_LOGGED", "VISIT", saved.getVisitId(),
                "Visit (" + purpose + ") logged for " + customer.getCustomerName() + " / " + vehicle.getRegistrationNumber()
                        + (createdCustomer ? " — new customer created" : "") + (createdVehicle ? " — new vehicle added" : "") + ".");

        VisitResponseDTO dto = toDto(saved, customerRepository.findById(customer.getCustomerId()).orElse(customer), vehicle);
        dto.setCreatedCustomer(createdCustomer);
        dto.setCreatedVehicle(createdVehicle);
        return dto;
    }

    public List<VisitResponseDTO> list() {
        List<Visit> visits = currentUserService.isSuperAdmin()
                ? visitRepository.findAllByOrderByVisitDateTimeDesc()
                : visitRepository.findByHandledByUserIdOrderByVisitDateTimeDesc(currentUserService.getCurrentUserId());
        return toDtos(visits);
    }

    public List<VisitResponseDTO> forCustomer(Long customerId) {
        return toDtos(visitRepository.findByCustomerIdOrderByVisitDateTimeDesc(customerId));
    }

    @Transactional
    public void delete(Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found with id: " + id));
        if (visit.getJobCardId() != null) {
            throw new IllegalArgumentException("This visit belongs to a job card — delete the job card instead");
        }
        visitRepository.delete(visit);
        statsService.recompute(visit.getCustomerId());
        auditLogService.record("VISIT_DELETED", "VISIT", id, "Visit on " + visit.getVisitDateTime() + " deleted.");
    }

    // ---- Job card link: every job card is a visit ----

    @Transactional
    public void recordJobCardVisit(JobCard jobCard) {
        if (jobCard == null || jobCard.getJobCardId() == null || visitRepository.existsByJobCardId(jobCard.getJobCardId())) return;
        Visit visit = new Visit();
        visit.setCustomerId(jobCard.getCustomerId());
        visit.setVehicleId(jobCard.getVehicleId());
        visit.setVisitDateTime(jobCard.getDateIn() != null ? jobCard.getDateIn()
                : jobCard.getCreatedAt() != null ? jobCard.getCreatedAt() : OffsetDateTime.now());
        visit.setPurpose("SERVICE");
        visit.setNotes("Job card " + (jobCard.getJobCardNumber() != null ? jobCard.getJobCardNumber() : "#" + jobCard.getJobCardId())
                + (jobCard.getComplaint() != null && !jobCard.getComplaint().isBlank() ? " — " + jobCard.getComplaint() : ""));
        Long handledBy = jobCard.getAdvisorUserId() != null ? jobCard.getAdvisorUserId() : jobCard.getTechnicianUserId();
        visit.setHandledByUserId(handledBy);
        visit.setHandledByName(handledBy != null ? userRepository.findById(handledBy).map(this::displayName).orElse(null) : null);
        visit.setSource("JOB_CARD");
        visit.setJobCardId(jobCard.getJobCardId());
        visitRepository.save(visit);
        statsService.recompute(jobCard.getCustomerId());
    }

    @Transactional
    public void removeJobCardVisit(Long jobCardId, Long customerId) {
        visitRepository.findByJobCardId(jobCardId).ifPresent(v -> {
            visitRepository.delete(v);
            statsService.recompute(customerId != null ? customerId : v.getCustomerId());
        });
    }

    // Job cards created before visit tracking existed each become a visit, once. Idempotent.
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void backfillFromJobCards() {
        boolean added = false;
        for (JobCard jc : jobCardRepository.findAll()) {
            if (jc.getCustomerId() == null || visitRepository.existsByJobCardId(jc.getJobCardId())) continue;
            recordJobCardVisit(jc);
            added = true;
        }
        // Statuses depend on "now" — refresh everyone on startup, not only customers just backfilled.
        statsService.recomputeAll();
        if (added) {
            auditLogService.record("VISITS_BACKFILLED", "VISIT", null, "Created visit records for existing job cards.");
        }
    }

    private String displayName(User u) {
        if (u.getFullName() != null && !u.getFullName().isBlank()) return u.getFullName();
        return u.getUsername();
    }

    private List<VisitResponseDTO> toDtos(List<Visit> visits) {
        Map<Long, Customer> customers = customerRepository.findAllById(
                visits.stream().map(Visit::getCustomerId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Customer::getCustomerId, Function.identity()));
        Map<Long, Vehicle> vehicles = vehicleRepository.findAllById(
                visits.stream().map(Visit::getVehicleId).filter(java.util.Objects::nonNull).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Vehicle::getVehicleId, Function.identity()));
        return visits.stream()
                .map(v -> toDto(v, customers.get(v.getCustomerId()), v.getVehicleId() != null ? vehicles.get(v.getVehicleId()) : null))
                .collect(Collectors.toList());
    }

    private VisitResponseDTO toDto(Visit v, Customer c, Vehicle vehicle) {
        VisitResponseDTO dto = new VisitResponseDTO();
        dto.setVisitId(v.getVisitId());
        dto.setCustomerId(v.getCustomerId());
        if (c != null) {
            dto.setCustomerName(c.getCustomerName());
            dto.setCustomerPhone(c.getPhone());
            dto.setCustomerRegularStatus(c.getRegularStatus());
            dto.setCustomerTotalVisits(c.getTotalVisits());
        }
        dto.setVehicleId(v.getVehicleId());
        if (vehicle != null) {
            dto.setVehicleModel(vehicle.getVehicleModel());
            dto.setRegistrationNumber(vehicle.getRegistrationNumber());
        }
        dto.setVisitDateTime(v.getVisitDateTime());
        dto.setPurpose(v.getPurpose());
        dto.setNotes(v.getNotes());
        dto.setHandledByUserId(v.getHandledByUserId());
        dto.setHandledByName(v.getHandledByName());
        dto.setSource(v.getSource());
        dto.setJobCardId(v.getJobCardId());
        dto.setCreatedAt(v.getCreatedAt());
        return dto;
    }
}
