package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.dto.MedicalRecordDTO;
import com.fpt.hivtreatment.exception.ResourceNotFoundException;
import com.fpt.hivtreatment.model.entity.MedicalRecord;
import com.fpt.hivtreatment.model.entity.TreatmentProtocol;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.repository.MedicalRecordRepository;
import com.fpt.hivtreatment.repository.TreatmentProtocolRepository;
import com.fpt.hivtreatment.repository.UserRepository;
import com.fpt.hivtreatment.service.MedicalRecordService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {

        @Autowired
        private MedicalRecordRepository medicalRecordRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private TreatmentProtocolRepository treatmentProtocolRepository;

        @Override
        @Transactional
        public MedicalRecordDTO createMedicalRecord(MedicalRecordDTO medicalRecordDTO) {
                // Find patient and doctor
                User patient = userRepository.findById(medicalRecordDTO.getPatientId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Patient not found with ID: " + medicalRecordDTO.getPatientId()));

                User doctor = null;
                if (medicalRecordDTO.getDoctorId() != null) {
                        doctor = userRepository.findById(medicalRecordDTO.getDoctorId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Doctor not found with ID: " + medicalRecordDTO.getDoctorId()));
                }

                // Find treatment protocol if provided
                TreatmentProtocol protocol = null;
                if (medicalRecordDTO.getPrimaryProtocolId() != null) {
                        protocol = treatmentProtocolRepository.findById(medicalRecordDTO.getPrimaryProtocolId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Treatment protocol not found with ID: "
                                                                        + medicalRecordDTO.getPrimaryProtocolId()));
                }

                MedicalRecord medicalRecord = MedicalRecord.builder()
                                .patient(patient)
                                .doctor(doctor)
                                .visitDate(medicalRecordDTO.getVisitDate())
                                .visitType(medicalRecordDTO.getVisitType())
                                .recordStatus(medicalRecordDTO.getRecordStatus())
                                .symptoms(medicalRecordDTO.getSymptoms())
                                .lymphNodes(medicalRecordDTO.getLymphNodes())
                                .bloodPressure(medicalRecordDTO.getBloodPressure())
                                .weight(medicalRecordDTO.getWeight() != null
                                                ? medicalRecordDTO.getWeight().doubleValue()
                                                : null)
                                .generalCondition(medicalRecordDTO.getGeneralCondition())
                                .diagnosis(medicalRecordDTO.getDiagnosis())
                                .primaryProtocol(protocol)
                                .protocolStartDate(medicalRecordDTO.getProtocolStartDate())
                                .whoClinicalStage(medicalRecordDTO.getWhoClinicalStage())
                                .opportunisticInfections(medicalRecordDTO.getOpportunisticInfections())
                                .protocolNotes(medicalRecordDTO.getProtocolNotes())
                                .riskFactors(medicalRecordDTO.getRiskFactors())
                                .nextAppointmentDate(medicalRecordDTO.getNextAppointmentDate())
                                .notes(medicalRecordDTO.getNotes())
                                .build();

                MedicalRecord savedRecord = medicalRecordRepository.save(medicalRecord);
                return mapToDTO(savedRecord);
        }

        @Override
        @Transactional
        public MedicalRecordDTO updateMedicalRecord(Long id, MedicalRecordDTO medicalRecordDTO) {
                MedicalRecord existingRecord = medicalRecordRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Medical record not found with ID: " + id));

                // Update the doctor if provided
                if (medicalRecordDTO.getDoctorId() != null &&
                                (existingRecord.getDoctor() == null
                                                || !existingRecord.getDoctor().getId()
                                                                .equals(medicalRecordDTO.getDoctorId()))) {
                        User doctor = userRepository.findById(medicalRecordDTO.getDoctorId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Doctor not found with ID: " + medicalRecordDTO.getDoctorId()));
                        existingRecord.setDoctor(doctor);
                }

                // Update the protocol if provided
                if (medicalRecordDTO.getPrimaryProtocolId() != null &&
                                (existingRecord.getPrimaryProtocol() == null
                                                || !existingRecord.getPrimaryProtocol().getId()
                                                                .equals(medicalRecordDTO.getPrimaryProtocolId()))) {
                        TreatmentProtocol protocol = treatmentProtocolRepository
                                        .findById(medicalRecordDTO.getPrimaryProtocolId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Treatment protocol not found with ID: "
                                                                        + medicalRecordDTO.getPrimaryProtocolId()));
                        existingRecord.setPrimaryProtocol(protocol);
                }

                // Update fields
                existingRecord.setVisitDate(medicalRecordDTO.getVisitDate());
                existingRecord.setVisitType(medicalRecordDTO.getVisitType());
                existingRecord.setRecordStatus(medicalRecordDTO.getRecordStatus());
                existingRecord.setSymptoms(medicalRecordDTO.getSymptoms());
                existingRecord.setLymphNodes(medicalRecordDTO.getLymphNodes());
                existingRecord.setBloodPressure(medicalRecordDTO.getBloodPressure());
                existingRecord.setWeight(
                                medicalRecordDTO.getWeight() != null ? medicalRecordDTO.getWeight().doubleValue()
                                                : null);
                existingRecord.setGeneralCondition(medicalRecordDTO.getGeneralCondition());
                existingRecord.setDiagnosis(medicalRecordDTO.getDiagnosis());
                existingRecord.setProtocolStartDate(medicalRecordDTO.getProtocolStartDate());
                existingRecord.setWhoClinicalStage(medicalRecordDTO.getWhoClinicalStage());
                existingRecord.setOpportunisticInfections(medicalRecordDTO.getOpportunisticInfections());
                existingRecord.setProtocolNotes(medicalRecordDTO.getProtocolNotes());
                existingRecord.setRiskFactors(medicalRecordDTO.getRiskFactors());
                existingRecord.setNextAppointmentDate(medicalRecordDTO.getNextAppointmentDate());
                existingRecord.setNotes(medicalRecordDTO.getNotes());

                MedicalRecord updatedRecord = medicalRecordRepository.save(existingRecord);
                return mapToDTO(updatedRecord);
        }

        @Override
        public MedicalRecordDTO getMedicalRecordById(Long id) {
                MedicalRecord medicalRecord = medicalRecordRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Medical record not found with ID: " + id));

                return mapToDTO(medicalRecord);
        }

        @Override
        public Map<String, Object> getMedicalRecordsByPatient(Long patientId, int page, int size) {
                User patient = userRepository.findById(patientId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Patient not found with ID: " + patientId));

                Page<MedicalRecord> recordsPage = medicalRecordRepository.findByPatient(
                                patient,
                                PageRequest.of(page, size, Sort.by("visitDate").descending()));

                List<MedicalRecordDTO> records = recordsPage.getContent()
                                .stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList());

                Map<String, Object> response = new HashMap<>();
                response.put("records", records);
                response.put("currentPage", recordsPage.getNumber());
                response.put("totalItems", recordsPage.getTotalElements());
                response.put("totalPages", recordsPage.getTotalPages());

                return response;
        }

        @Override
        public Map<String, Object> getMedicalRecordsByDoctor(Long doctorId, int page, int size) {
                User doctor = userRepository.findById(doctorId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor not found with ID: " + doctorId));

                Page<MedicalRecord> recordsPage = medicalRecordRepository.findByDoctor(
                                doctor,
                                PageRequest.of(page, size, Sort.by("visitDate").descending()));

                List<MedicalRecordDTO> records = recordsPage.getContent()
                                .stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList());

                Map<String, Object> response = new HashMap<>();
                response.put("records", records);
                response.put("currentPage", recordsPage.getNumber());
                response.put("totalItems", recordsPage.getTotalElements());
                response.put("totalPages", recordsPage.getTotalPages());

                return response;
        }

        @Override
        public List<MedicalRecordDTO> getMedicalRecordsByPatientAndDateRange(Long patientId, LocalDate startDate,
                        LocalDate endDate) {
                List<MedicalRecord> records = medicalRecordRepository.findByPatientIdAndDateRange(patientId, startDate,
                                endDate);

                return records.stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public List<MedicalRecordDTO> getMedicalRecordsByDoctorAndDate(Long doctorId, LocalDate date) {
                List<MedicalRecord> records = medicalRecordRepository.findByDoctorIdAndDate(doctorId, date);

                return records.stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public void deleteMedicalRecord(Long id) {
                if (!medicalRecordRepository.existsById(id)) {
                        throw new ResourceNotFoundException("Medical record not found with ID: " + id);
                }

                medicalRecordRepository.deleteById(id);
        }

        @Override
        @Transactional
        public MedicalRecordDTO updateMedicalRecordStatus(Long id, String status) {
                MedicalRecord record = medicalRecordRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Medical record not found with ID: " + id));

                record.setRecordStatus(status);
                MedicalRecord updatedRecord = medicalRecordRepository.save(record);

                return mapToDTO(updatedRecord);
        }

        @Override
        public long countMedicalRecordsByStatus(String status) {
                return medicalRecordRepository.countByRecordStatus(status);
        }

        /**
         * Maps a MedicalRecord entity to a MedicalRecordDTO
         */
        private MedicalRecordDTO mapToDTO(MedicalRecord record) {
                return MedicalRecordDTO.builder()
                                .id(record.getId())
                                .patientId(record.getPatient().getId())
                                .patientName(record.getPatient().getFullName())
                                .doctorId(record.getDoctor() != null ? record.getDoctor().getId() : null)
                                .doctorName(record.getDoctor() != null ? record.getDoctor().getFullName() : null)
                                .visitDate(record.getVisitDate())
                                .visitType(record.getVisitType())
                                .recordStatus(record.getRecordStatus())
                                .symptoms(record.getSymptoms())
                                .lymphNodes(record.getLymphNodes())
                                .bloodPressure(record.getBloodPressure())
                                .weight(record.getWeight() != null ? new java.math.BigDecimal(record.getWeight())
                                                : null)
                                .generalCondition(record.getGeneralCondition())
                                .diagnosis(record.getDiagnosis())
                                .primaryProtocolId(record.getPrimaryProtocol() != null
                                                ? record.getPrimaryProtocol().getId()
                                                : null)
                                .protocolName(record.getPrimaryProtocol() != null
                                                ? record.getPrimaryProtocol().getName()
                                                : null)
                                .protocolStartDate(record.getProtocolStartDate())
                                .whoClinicalStage(record.getWhoClinicalStage())
                                .opportunisticInfections(record.getOpportunisticInfections())
                                .protocolNotes(record.getProtocolNotes())
                                .riskFactors(record.getRiskFactors())
                                .nextAppointmentDate(record.getNextAppointmentDate())
                                .notes(record.getNotes())
                                .createdAt(record.getCreatedAt())
                                .updatedAt(record.getUpdatedAt())
                                .build();
        }
}