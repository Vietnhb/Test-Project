package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.model.entity.Medication;
import com.fpt.hivtreatment.model.entity.ProtocolMedication;
import com.fpt.hivtreatment.model.entity.TreatmentProtocol;
import com.fpt.hivtreatment.repository.MedicationRepository;
import com.fpt.hivtreatment.repository.ProtocolMedicationRepository;
import com.fpt.hivtreatment.repository.TreatmentProtocolRepository;
import com.fpt.hivtreatment.service.TreatmentProtocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TreatmentProtocolServiceImpl implements TreatmentProtocolService {

    private final TreatmentProtocolRepository treatmentProtocolRepository;
    private final ProtocolMedicationRepository protocolMedicationRepository;
    private final MedicationRepository medicationRepository;

    @Autowired
    public TreatmentProtocolServiceImpl(
            TreatmentProtocolRepository treatmentProtocolRepository,
            ProtocolMedicationRepository protocolMedicationRepository,
            MedicationRepository medicationRepository) {
        this.treatmentProtocolRepository = treatmentProtocolRepository;
        this.protocolMedicationRepository = protocolMedicationRepository;
        this.medicationRepository = medicationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentProtocol> getAllProtocols() {
        // Fetch all protocols from the database with all fields
        List<TreatmentProtocol> protocols = treatmentProtocolRepository.findAll();

        // Log the result for debugging
        if (protocols.isEmpty()) {
            System.out.println("No treatment protocols found in database");
        } else {
            System.out.println("Found " + protocols.size() + " treatment protocols");
            protocols.forEach(
                    p -> System.out.println("Protocol: " + p.getId() + " - " + p.getName() + " - " + p.getCode()));
        }

        return protocols;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TreatmentProtocol> getProtocolById(Long id) {
        Optional<TreatmentProtocol> protocol = treatmentProtocolRepository.findById(id);
        protocol.ifPresentOrElse(
                p -> System.out.println("Found protocol by ID " + id + ": " + p.getName()),
                () -> System.out.println("Protocol with ID " + id + " not found"));
        return protocol;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentProtocol> getProtocolsByCategory(String category) {
        List<TreatmentProtocol> protocols = treatmentProtocolRepository.findByCategory(category);
        System.out.println("Found " + protocols.size() + " protocols for category: " + category);
        return protocols;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentProtocol> getProtocolsByIndication(String indication) {
        List<TreatmentProtocol> protocols = treatmentProtocolRepository.findByIndication(indication);
        System.out.println("Found " + protocols.size() + " protocols for indication: " + indication);
        return protocols;
    }

    @Override
    @Transactional
    public TreatmentProtocol saveProtocol(TreatmentProtocol protocol) {
        // Set updated timestamp
        protocol.setUpdatedAt(LocalDateTime.now());
        // If it's a new protocol, set created timestamp
        if (protocol.getId() == null) {
            protocol.setCreatedAt(LocalDateTime.now());
        }

        TreatmentProtocol savedProtocol = treatmentProtocolRepository.save(protocol);
        System.out.println("Saved protocol with ID: " + savedProtocol.getId());
        return savedProtocol;
    }

    @Override
    @Transactional
    public void deleteProtocol(Long id) {
        System.out.println("Deleting protocol with ID: " + id);
        treatmentProtocolRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Medication> getMedicationsByProtocolId(Long protocolId) {
        System.out.println("Service: Starting getMedicationsByProtocolId for protocol ID: " + protocolId);

        // First check if the protocol exists
        if (!treatmentProtocolRepository.existsById(protocolId)) {
            System.out.println("Service: Protocol ID " + protocolId + " does not exist");
            return new ArrayList<>();
        }
        System.out.println("Service: Protocol ID " + protocolId + " exists");

        // Get all protocol medications for this protocol
        List<ProtocolMedication> protocolMedications = protocolMedicationRepository.findByProtocolId(protocolId);
        System.out.println("Service: Found " + protocolMedications.size()
                + " protocol_medication entries for protocol ID: " + protocolId);

        // Extract medications directly from protocol medications (due to JOIN FETCH)
        List<Medication> medications = protocolMedications.stream()
                .map(ProtocolMedication::getMedication)
                .collect(Collectors.toList());

        System.out.println("Service: Extracted " + medications.size() + " medications for protocol ID: " + protocolId);

        // Log medication details for debugging
        medications.forEach(med -> System.out
                .println("  - Medication: " + med.getId() + " - " + med.getName() + " (" + med.getGenericName() + ")"));

        return medications;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<TreatmentProtocol, List<Medication>> getAllProtocolsWithMedications() {
        Map<TreatmentProtocol, List<Medication>> result = new HashMap<>();

        // Get all protocols
        List<TreatmentProtocol> protocols = treatmentProtocolRepository.findAll();
        System.out.println("Getting medications for " + protocols.size() + " protocols");

        if (protocols.isEmpty()) {
            return result;
        }

        // Get all protocol medications in one query for better performance
        List<ProtocolMedication> allProtocolMedications = protocolMedicationRepository.findAll();
        System.out.println("Total protocol-medication mappings found: " + allProtocolMedications.size());

        if (allProtocolMedications.isEmpty()) {
            // If no medication mappings exist, return protocols with empty medication lists
            protocols.forEach(p -> result.put(p, new ArrayList<>()));
            return result;
        }

        // Group protocol medications by protocol ID
        Map<Long, List<ProtocolMedication>> protocolMedicationMap = allProtocolMedications.stream()
                .collect(Collectors.groupingBy(pm -> pm.getProtocol().getId()));

        // Get all medication IDs needed
        List<Long> allMedicationIds = allProtocolMedications.stream()
                .map(pm -> pm.getMedication().getId())
                .distinct()
                .collect(Collectors.toList());

        // Fetch all required medications at once
        Map<Long, Medication> medicationMap = new HashMap<>();
        if (!allMedicationIds.isEmpty()) {
            medicationRepository.findAllById(allMedicationIds).forEach(med -> medicationMap.put(med.getId(), med));
        }

        // Map protocols to their medications
        for (TreatmentProtocol protocol : protocols) {
            List<ProtocolMedication> protocolMeds = protocolMedicationMap.getOrDefault(protocol.getId(),
                    new ArrayList<>());
            List<Medication> medications = protocolMeds.stream()
                    .map(pm -> medicationMap.get(pm.getMedication().getId()))
                    .filter(med -> med != null) // Filter out any null values
                    .collect(Collectors.toList());

            result.put(protocol, medications);
        }

        return result;
    }
}