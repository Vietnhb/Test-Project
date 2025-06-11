package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.dto.MedicalRecordDTO;
import com.fpt.hivtreatment.model.entity.MedicalRecord;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.repository.MedicalRecordRepository;
import com.fpt.hivtreatment.repository.UserRepository;
import com.fpt.hivtreatment.service.impl.MedicalRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for MedicalRecordService to verify medical record data saving
 */
@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    private User mockPatient;
    private User mockDoctor;
    private MedicalRecordDTO testMedicalRecordDTO;

    @BeforeEach
    void setUp() {
        // Setup mock patient
        mockPatient = new User();
        mockPatient.setId(1L);
        mockPatient.setFullName("John Doe");

        // Setup mock doctor
        mockDoctor = new User();
        mockDoctor.setId(2L);
        mockDoctor.setFullName("Dr. Smith");

        // Setup test medical record data (matching ExaminationForm data structure)
        testMedicalRecordDTO = MedicalRecordDTO.builder()
                .patientId(1L)
                .doctorId(2L)
                .visitDate(LocalDate.now())
                .visitType("Khám định kỳ")
                .recordStatus("Đang điều trị")
                .symptoms("Đau đầu, mệt mỏi")
                .lymphNodes("Hạch bạch huyết bình thường")
                .bloodPressure("120/80")
                .weight(new BigDecimal("70.5"))
                .generalCondition("Tốt")
                .diagnosis("HIV Stage 1")
                .riskFactors("Quan hệ tình dục không an toàn")
                .whoClinicalStage("Stage 1")
                .opportunisticInfections("Không")
                .protocolNotes("Bệnh nhân tuân thủ điều trị tốt")
                .notes("Tiếp tục theo dõi định kỳ")
                .build();
    }

    @Test
    void testCreateMedicalRecord_ShouldSaveCorrectly() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockPatient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockDoctor));
        
        MedicalRecord savedRecord = createMockSavedRecord();
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(savedRecord);

        // When
        MedicalRecordDTO result = medicalRecordService.createMedicalRecord(testMedicalRecordDTO);

        // Then
        verify(userRepository).findById(1L); // Patient lookup
        verify(userRepository).findById(2L); // Doctor lookup
        
        ArgumentCaptor<MedicalRecord> recordCaptor = ArgumentCaptor.forClass(MedicalRecord.class);
        verify(medicalRecordRepository).save(recordCaptor.capture());
        
        MedicalRecord capturedRecord = recordCaptor.getValue();
        
        // Verify all critical fields are mapped correctly
        assertEquals(mockPatient, capturedRecord.getPatient());
        assertEquals(mockDoctor, capturedRecord.getDoctor());
        assertEquals("Khám định kỳ", capturedRecord.getVisitType());
        assertEquals("Đang điều trị", capturedRecord.getRecordStatus());
        assertEquals("Đau đầu, mệt mỏi", capturedRecord.getSymptoms());
        assertEquals("Hạch bạch huyết bình thường", capturedRecord.getLymphNodes());
        assertEquals("120/80", capturedRecord.getBloodPressure());
        assertEquals(70.5, capturedRecord.getWeight(), 0.01);
        assertEquals("Tốt", capturedRecord.getGeneralCondition());
        assertEquals("HIV Stage 1", capturedRecord.getDiagnosis());
        assertEquals("Quan hệ tình dục không an toàn", capturedRecord.getRiskFactors());
        assertEquals("Stage 1", capturedRecord.getWhoClinicalStage());
        assertEquals("Không", capturedRecord.getOpportunisticInfections());
        assertEquals("Bệnh nhân tuân thủ điều trị tốt", capturedRecord.getProtocolNotes());
        assertEquals("Tiếp tục theo dõi định kỳ", capturedRecord.getNotes());
        
        // Verify result DTO
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getPatientName());
        assertEquals("Dr. Smith", result.getDoctorName());
    }

    @Test
    void testCreateMedicalRecord_WithNumericFields() {
        // Test specific numeric field conversion
        testMedicalRecordDTO.setWeight(new BigDecimal("65.25"));
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockPatient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockDoctor));
        
        MedicalRecord savedRecord = createMockSavedRecord();
        savedRecord.setWeight(65.25);
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(savedRecord);

        // When
        MedicalRecordDTO result = medicalRecordService.createMedicalRecord(testMedicalRecordDTO);

        // Then
        ArgumentCaptor<MedicalRecord> recordCaptor = ArgumentCaptor.forClass(MedicalRecord.class);
        verify(medicalRecordRepository).save(recordCaptor.capture());
        
        MedicalRecord capturedRecord = recordCaptor.getValue();
        assertEquals(65.25, capturedRecord.getWeight(), 0.01);
        
        // Verify BigDecimal conversion in result
        assertEquals(new BigDecimal("65.25"), result.getWeight());
    }

    @Test
    void testCreateMedicalRecord_WithNullOptionalFields() {
        // Test with null weight (optional field)
        testMedicalRecordDTO.setWeight(null);
        testMedicalRecordDTO.setDoctorId(null);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockPatient));
        
        MedicalRecord savedRecord = createMockSavedRecord();
        savedRecord.setWeight(null);
        savedRecord.setDoctor(null);
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(savedRecord);

        // When
        MedicalRecordDTO result = medicalRecordService.createMedicalRecord(testMedicalRecordDTO);

        // Then
        ArgumentCaptor<MedicalRecord> recordCaptor = ArgumentCaptor.forClass(MedicalRecord.class);
        verify(medicalRecordRepository).save(recordCaptor.capture());
        
        MedicalRecord capturedRecord = recordCaptor.getValue();
        assertNull(capturedRecord.getWeight());
        assertNull(capturedRecord.getDoctor());
        
        verify(userRepository, never()).findById(2L); // Should not lookup doctor
    }

    private MedicalRecord createMockSavedRecord() {
        return MedicalRecord.builder()
                .id(1L)
                .patient(mockPatient)
                .doctor(mockDoctor)
                .visitDate(LocalDate.now())
                .visitType("Khám định kỳ")
                .recordStatus("Đang điều trị")
                .symptoms("Đau đầu, mệt mỏi")
                .lymphNodes("Hạch bạch huyết bình thường")
                .bloodPressure("120/80")
                .weight(70.5)
                .generalCondition("Tốt")
                .diagnosis("HIV Stage 1")
                .riskFactors("Quan hệ tình dục không an toàn")
                .whoClinicalStage("Stage 1")
                .opportunisticInfections("Không")
                .protocolNotes("Bệnh nhân tuân thủ điều trị tốt")
                .notes("Tiếp tục theo dõi định kỳ")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
