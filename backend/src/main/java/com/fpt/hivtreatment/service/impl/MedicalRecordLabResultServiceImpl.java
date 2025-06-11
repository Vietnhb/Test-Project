package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.model.entity.LabTestResult;
import com.fpt.hivtreatment.model.entity.MedicalRecord;
import com.fpt.hivtreatment.model.entity.MedicalRecordLabResult;
import com.fpt.hivtreatment.repository.LabTestResultRepository;
import com.fpt.hivtreatment.repository.MedicalRecordLabResultRepository;
import com.fpt.hivtreatment.repository.MedicalRecordRepository;
import com.fpt.hivtreatment.service.MedicalRecordLabResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MedicalRecordLabResultServiceImpl implements MedicalRecordLabResultService {

    @Autowired
    private MedicalRecordLabResultRepository medicalRecordLabResultRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private LabTestResultRepository labTestResultRepository;

    @Override
    public MedicalRecordLabResult createAssociation(Long medicalRecordId, Long labTestResultId,
            String resultInterpretation, String clinicalSignificance) {
        // Find medical record
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found with id: " + medicalRecordId));

        // Find lab test result
        LabTestResult labTestResult = labTestResultRepository.findById(labTestResultId)
                .orElseThrow(() -> new RuntimeException("Lab test result not found with id: " + labTestResultId));

        // Check if association already exists
        if (medicalRecordLabResultRepository.existsByMedicalRecordIdAndLabTestResultId(medicalRecordId,
                labTestResultId)) {
            throw new RuntimeException("Association already exists between this medical record and lab test result");
        }

        // Create association
        MedicalRecordLabResult association = new MedicalRecordLabResult();
        association.setMedicalRecord(medicalRecord);
        association.setLabTestResult(labTestResult);
        association.setResultInterpretation(resultInterpretation);
        association.setCreatedAt(LocalDateTime.now());

        return medicalRecordLabResultRepository.save(association);
    }

    @Override
    public List<MedicalRecordLabResult> findByMedicalRecordId(Long medicalRecordId) {
        return medicalRecordLabResultRepository.findByMedicalRecordId(medicalRecordId);
    }

    @Override
    public List<MedicalRecordLabResult> findByLabTestResultId(Long labTestResultId) {
        return medicalRecordLabResultRepository.findByLabTestResultId(labTestResultId);
    }

    @Override
    public Optional<MedicalRecordLabResult> findByMedicalRecordIdAndLabTestResultId(Long medicalRecordId,
            Long labTestResultId) {
        return medicalRecordLabResultRepository.findByMedicalRecordIdAndLabTestResultId(medicalRecordId,
                labTestResultId);
    }

    @Override
    public MedicalRecordLabResult updateInterpretation(Long medicalRecordId, Long labTestResultId,
            String resultInterpretation, String clinicalSignificance) {
        // Find the association
        MedicalRecordLabResult association = medicalRecordLabResultRepository
                .findByMedicalRecordIdAndLabTestResultId(medicalRecordId, labTestResultId)
                .orElseThrow(() -> new RuntimeException(
                        "Association not found between medical record id: " + medicalRecordId +
                                " and lab test result id: " + labTestResultId));

        // Update fields
        association.setResultInterpretation(resultInterpretation);

        return medicalRecordLabResultRepository.save(association);
    }

    @Override
    public List<LabTestResult> findLabTestResultsByMedicalRecordId(Long medicalRecordId) {
        return medicalRecordLabResultRepository.findLabTestResultsByMedicalRecordId(medicalRecordId);
    }

    @Override
    public List<MedicalRecord> findMedicalRecordsByLabTestResultId(Long labTestResultId) {
        return medicalRecordLabResultRepository.findMedicalRecordsByLabTestResultId(labTestResultId);
    }

    @Override
    public void deleteAssociation(Long medicalRecordId, Long labTestResultId) {
        Optional<MedicalRecordLabResult> associationOpt = medicalRecordLabResultRepository
                .findByMedicalRecordIdAndLabTestResultId(medicalRecordId, labTestResultId);

        if (associationOpt.isPresent()) {
            medicalRecordLabResultRepository.delete(associationOpt.get());
        } else {
            throw new RuntimeException("Association not found between medical record id: " + medicalRecordId +
                    " and lab test result id: " + labTestResultId);
        }
    }

    @Override
    public void deleteAllByMedicalRecordId(Long medicalRecordId) {
        List<MedicalRecordLabResult> associations = medicalRecordLabResultRepository
                .findByMedicalRecordId(medicalRecordId);
        medicalRecordLabResultRepository.deleteAll(associations);
    }

    @Override
    public void deleteAllByLabTestResultId(Long labTestResultId) {
        List<MedicalRecordLabResult> associations = medicalRecordLabResultRepository
                .findByLabTestResultId(labTestResultId);
        medicalRecordLabResultRepository.deleteAll(associations);
    }
}