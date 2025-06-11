package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.model.entity.PrescriptionItem;
import com.fpt.hivtreatment.repository.PrescriptionItemRepository;
import com.fpt.hivtreatment.service.PrescriptionItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrescriptionItemServiceImpl implements PrescriptionItemService {

    private final PrescriptionItemRepository prescriptionItemRepository;

    @Override
    public PrescriptionItem createPrescriptionItem(PrescriptionItem prescriptionItem) {
        log.info("Creating prescription item for prescription ID: {}, medication ID: {}",
                prescriptionItem.getPrescriptionId(), prescriptionItem.getMedicationId());        // Tính tổng liều hàng ngày
        prescriptionItem.setDailyTotal(calculateDailyTotal(prescriptionItem));

        PrescriptionItem savedItem = prescriptionItemRepository.save(prescriptionItem);
        log.info("Created prescription item with ID: {}", savedItem.getId());

        return savedItem;
    }

    @Override
    public List<PrescriptionItem> createPrescriptionItems(List<PrescriptionItem> prescriptionItems) {
        log.info("Creating {} prescription items", prescriptionItems.size());

        List<PrescriptionItem> savedItems = new ArrayList<>();
        for (PrescriptionItem item : prescriptionItems) {
            savedItems.add(createPrescriptionItem(item));
        }

        return savedItems;
    }

    @Override
    public PrescriptionItem updatePrescriptionItem(Long id, PrescriptionItem prescriptionItem) {
        log.info("Updating prescription item with ID: {}", id);

        PrescriptionItem existingItem = findById(id);
          // Cập nhật các trường
        existingItem.setMedicationId(prescriptionItem.getMedicationId());
        existingItem.setMorningDose(prescriptionItem.getMorningDose());
        existingItem.setNoonDose(prescriptionItem.getNoonDose());
        existingItem.setAfternoonDose(prescriptionItem.getAfternoonDose());
        existingItem.setEveningDose(prescriptionItem.getEveningDose());
        existingItem.setUsageInstructions(prescriptionItem.getUsageInstructions());
        existingItem.setSpecialNotes(prescriptionItem.getSpecialNotes());

        // Tính lại tổng liều hàng ngày
        existingItem.setDailyTotal(calculateDailyTotal(existingItem));

        return prescriptionItemRepository.save(existingItem);
    }

    @Override
    public void deletePrescriptionItem(Long id) {
        log.info("Deleting prescription item with ID: {}", id);
        prescriptionItemRepository.deleteById(id);
    }

    @Override
    public PrescriptionItem findById(Long id) {
        return prescriptionItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription item not found with ID: " + id));
    }

    @Override
    public List<PrescriptionItem> findByPrescriptionId(Long prescriptionId) {
        return prescriptionItemRepository.findByPrescriptionId(prescriptionId);
    }

    @Override
    public List<PrescriptionItem> findByPatientId(Long patientId) {
        return prescriptionItemRepository.findByPatientId(patientId);
    }

    @Override
    public List<PrescriptionItem> findByMedicationId(Long medicationId) {
        return prescriptionItemRepository.findByMedicationId(medicationId);
    }

    @Override
    public List<PrescriptionItem> findActiveByPatientId(Long patientId) {
        return prescriptionItemRepository.findActiveByPatientId(patientId);
    }    @Override
    public List<PrescriptionItem> findActiveByMedicationId(Long medicationId) {
        return prescriptionItemRepository.findByMedicationId(medicationId);
    }    @Override
    public PrescriptionItem updateStatus(Long id, String status) {
        log.info("Updating status of prescription item ID: {} to: {}", id, status);
        
        PrescriptionItem item = findById(id);
        // Note: Status field not available in current entity, this is a placeholder
        // item.setStatus(status);
        
        return prescriptionItemRepository.save(item);
    }@Override
    public Integer calculateDailyTotal(PrescriptionItem prescriptionItem) {
        int total = 0;
        
        if (prescriptionItem.getMorningDose() != null) {
            total += prescriptionItem.getMorningDose();
        }
        if (prescriptionItem.getNoonDose() != null) {
            total += prescriptionItem.getNoonDose();
        }
        if (prescriptionItem.getAfternoonDose() != null) {
            total += prescriptionItem.getAfternoonDose();
        }
        if (prescriptionItem.getEveningDose() != null) {
            total += prescriptionItem.getEveningDose();
        }
        
        return total;
    }
}
