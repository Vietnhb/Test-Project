package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.dto.DoctorScheduleDTO;
import com.fpt.hivtreatment.exception.ResourceNotFoundException;
import com.fpt.hivtreatment.model.entity.DoctorProfile;
import com.fpt.hivtreatment.model.entity.DoctorSchedule;
import com.fpt.hivtreatment.model.entity.WorkShift;
import com.fpt.hivtreatment.payload.request.DoctorScheduleRequest;
import com.fpt.hivtreatment.payload.response.DoctorScheduleResponse;
import com.fpt.hivtreatment.repository.DoctorProfileRepository;
import com.fpt.hivtreatment.repository.DoctorScheduleRepository;
import com.fpt.hivtreatment.repository.WorkShiftRepository;
import com.fpt.hivtreatment.repository.AppointmentSlotRepository;
import com.fpt.hivtreatment.service.DoctorScheduleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

        private final DoctorScheduleRepository doctorScheduleRepository;
        private final DoctorProfileRepository doctorProfileRepository;
        private final WorkShiftRepository workShiftRepository;
        private final AppointmentSlotRepository appointmentSlotRepository;
        private static final Logger logger = LoggerFactory.getLogger(DoctorScheduleServiceImpl.class);

        @Override
        @Transactional
        public DoctorScheduleResponse createSchedule(DoctorScheduleRequest request) {
                // Get the doctor
                DoctorProfile doctor = doctorProfileRepository.findById(request.getDoctorId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor not found with id: " + request.getDoctorId()));

                // Get the work shift that matches the start and end time
                WorkShift workShift = workShiftRepository.findById(1L) // Default to first work shift for now
                                .orElseThrow(() -> new ResourceNotFoundException("Work shift not found"));

                // Validate the time range
                if (request.getStartTime().isAfter(request.getEndTime())) {
                        throw new IllegalArgumentException("Start time must be before end time");
                }

                // Create the schedule
                DoctorSchedule schedule = DoctorSchedule.builder()
                                .doctor(doctor)
                                .scheduleDate(request.getScheduleDate())
                                .workShift(workShift)
                                .build();

                DoctorSchedule savedSchedule = doctorScheduleRepository.save(schedule);

                return mapToDoctorScheduleResponse(savedSchedule);
        }

        @Override
        public List<DoctorScheduleResponse> getSchedulesByDoctor(Long doctorId) {
                DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor not found with id: " + doctorId));

                List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctor(doctor);

                return schedules.stream()
                                .map(this::mapToDoctorScheduleResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public List<DoctorScheduleResponse> getSchedulesByDoctorAndDate(Long doctorId, LocalDate date) {
                DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor not found with id: " + doctorId));

                List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorAndScheduleDate(doctor, date);

                return schedules.stream()
                                .map(this::mapToDoctorScheduleResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public List<DoctorScheduleResponse> getFutureSchedules() {
                LocalDate today = LocalDate.now();
                List<DoctorSchedule> schedules = doctorScheduleRepository.findByScheduleDateGreaterThanEqual(today);

                return schedules.stream()
                                .map(this::mapToDoctorScheduleResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public DoctorSchedule getScheduleById(Long scheduleId) {
                return doctorScheduleRepository.findById(scheduleId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor schedule not found with id: " + scheduleId));
        }

        @Override
        public DoctorScheduleDTO createDoctorSchedule(DoctorScheduleDTO scheduleDTO) {
                logger.info("Creating doctor schedule: {}", scheduleDTO);

                try {
                        // Lấy thông tin bác sĩ từ doctor_id
                        Long doctorId = scheduleDTO.getDoctorId();
                        logger.info("Tìm kiếm bác sĩ với ID: {}", doctorId);

                        // Kiểm tra tất cả doctorId hiện có trong DB
                        List<DoctorProfile> allDoctors = doctorProfileRepository.findAll();
                        logger.info("Có {} bác sĩ trong database", allDoctors.size());
                        allDoctors.forEach(doc -> logger.info("Doctor in DB: ID={}", doc.getDoctorId()));

                        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                                        .orElseThrow(() -> {
                                                logger.error("Không tìm thấy bác sĩ với ID: {}", doctorId);
                                                return new IllegalArgumentException(
                                                                "Không tìm thấy bác sĩ với ID: " + doctorId);
                                        });

                        // Lấy thông tin ca làm việc từ work_shift_id
                        Long workShiftId = scheduleDTO.getWorkShiftId();
                        WorkShift workShift = workShiftRepository.findById(workShiftId)
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Không tìm thấy ca làm việc với ID: " + workShiftId));

                        // Parse ngày từ chuỗi
                        LocalDate scheduleDate = scheduleDTO.getScheduleDate();
                        if (scheduleDate == null) {
                                logger.error("Lỗi: Ngày lịch làm việc không được để trống");
                                throw new IllegalArgumentException("Ngày lịch làm việc không được để trống");
                        }

                        // Kiểm tra xem đã có lịch cho ngày và bác sĩ này chưa
                        List<DoctorSchedule> existingSchedules = doctorScheduleRepository
                                        .findByDoctorAndScheduleDate(doctor, scheduleDate);
                        if (!existingSchedules.isEmpty()) {
                                logger.warn("Đã tồn tại lịch làm việc cho bác sĩ {} vào ngày {}", doctorId,
                                                scheduleDate);
                                // Throw exception to prevent multiple schedules per day for a doctor
                                throw new IllegalArgumentException("Đã tồn tại lịch làm việc cho bác sĩ này vào ngày "
                                                + scheduleDate
                                                + ". Mỗi bác sĩ chỉ được phép có 1 lịch làm việc trong 1 ngày.");
                        }

                        // Tạo đối tượng schedule mới
                        DoctorSchedule schedule = new DoctorSchedule();
                        schedule.setDoctor(doctor);
                        schedule.setScheduleDate(scheduleDate);
                        schedule.setWorkShift(workShift);
                        schedule.setNotes(scheduleDTO.getNotes());

                        // Lưu vào database
                        DoctorSchedule savedSchedule = doctorScheduleRepository.save(schedule);
                        logger.info("Đã tạo thành công lịch làm việc ID: {}", savedSchedule.getId());

                        return mapToDoctorScheduleDTO(savedSchedule);
                } catch (Exception e) {
                        logger.error("Lỗi khi tạo lịch làm việc cho bác sĩ", e);
                        if (e instanceof IllegalArgumentException) {
                                throw e;
                        }
                        throw new RuntimeException("Lỗi khi tạo lịch làm việc: " + e.getMessage(), e);
                }
        }

        @Override
        public List<DoctorScheduleDTO> getDoctorSchedules(Long doctorId, String date, int page, int size) {
                Pageable pageable = PageRequest.of(page, size);
                List<DoctorSchedule> schedules = new ArrayList<>();

                // Nếu có doctorId và date
                if (doctorId != null && date != null) {
                        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Doctor not found with id: " + doctorId));
                        LocalDate localDate = LocalDate.parse(date);
                        schedules = doctorScheduleRepository.findByDoctorAndScheduleDate(doctor, localDate);
                }
                // Nếu chỉ có doctorId
                else if (doctorId != null) {
                        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Doctor not found with id: " + doctorId));
                        schedules = doctorScheduleRepository.findByDoctor(doctor);
                }
                // Nếu chỉ có date
                else if (date != null) {
                        LocalDate localDate = LocalDate.parse(date);
                        schedules = doctorScheduleRepository.findByScheduleDate(localDate);
                }
                // Không có bộ lọc nào
                else {
                        schedules = doctorScheduleRepository.findAll();
                }

                return schedules.stream()
                                .map(this::mapToDoctorScheduleDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public List<DoctorScheduleDTO> getDoctorSchedulesByDateRange(Long doctorId, String startDate, String endDate) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate start = LocalDate.parse(startDate, formatter);
                LocalDate end = LocalDate.parse(endDate, formatter);

                DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor not found with id: " + doctorId));

                List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorAndScheduleDateBetween(doctor,
                                start, end);

                return schedules.stream()
                                .map(this::mapToDoctorScheduleDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public long countDoctorSchedules(Long doctorId, String date) {
                // Nếu có doctorId và date
                if (doctorId != null && date != null) {
                        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Doctor not found with id: " + doctorId));
                        LocalDate localDate = LocalDate.parse(date);
                        return doctorScheduleRepository.countByDoctorAndScheduleDate(doctor, localDate);
                }
                // Nếu chỉ có doctorId
                else if (doctorId != null) {
                        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Doctor not found with id: " + doctorId));
                        return doctorScheduleRepository.countByDoctor(doctor);
                }
                // Nếu chỉ có date
                else if (date != null) {
                        LocalDate localDate = LocalDate.parse(date);
                        return doctorScheduleRepository.countByScheduleDate(localDate);
                }
                // Không có bộ lọc nào
                else {
                        return doctorScheduleRepository.count();
                }
        }

        @Override
        public DoctorScheduleDTO getDoctorScheduleById(Long id) {
                DoctorSchedule schedule = doctorScheduleRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor schedule not found with id: " + id));
                return mapToDoctorScheduleDTO(schedule);
        }

        @Override
        public DoctorScheduleDTO updateDoctorSchedule(Long id, DoctorScheduleDTO scheduleDTO) {
                DoctorSchedule schedule = doctorScheduleRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor schedule not found with id: " + id));

                // Get the work shift that matches the start and end time
                WorkShift workShift = workShiftRepository.findById(1L) // Default to first work shift for now
                                .orElseThrow(() -> new ResourceNotFoundException("Work shift not found"));

                // Validate the time range
                if (scheduleDTO.getStartTime().isAfter(scheduleDTO.getEndTime())) {
                        throw new IllegalArgumentException("Start time must be before end time");
                }

                // Update the schedule
                schedule.setScheduleDate(scheduleDTO.getScheduleDate());
                schedule.setWorkShift(workShift);

                DoctorSchedule updatedSchedule = doctorScheduleRepository.save(schedule);
                return mapToDoctorScheduleDTO(updatedSchedule);
        }

        @Override
        @Transactional
        public void deleteDoctorSchedule(Long id) {
                DoctorSchedule schedule = doctorScheduleRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Doctor schedule not found with id: " + id));

                // Trước khi xóa lịch làm việc, cần xóa các appointment slots liên quan
                try {
                        logger.info("Xóa các appointment slots liên quan đến lịch làm việc ID: {}", id);
                        // Gọi phương thức xóa appointment slots theo doctorScheduleId
                        appointmentSlotRepository.deleteByDoctorScheduleId(id);
                        logger.info("Đã xóa appointment slots thành công");
                } catch (Exception e) {
                        logger.error("Lỗi khi xóa appointment slots: {}", e.getMessage(), e);
                        throw new RuntimeException("Lỗi khi xóa appointment slots: " + e.getMessage(), e);
                }

                // Xóa lịch làm việc
                logger.info("Xóa lịch làm việc ID: {}", id);
                doctorScheduleRepository.delete(schedule);
                logger.info("Đã xóa lịch làm việc thành công");
        }

        // Phương thức hỗ trợ
        private DoctorScheduleResponse mapToDoctorScheduleResponse(DoctorSchedule schedule) {
                return DoctorScheduleResponse.builder()
                                .id(schedule.getId())
                                .doctorId(schedule.getDoctor().getDoctorId())
                                .doctorName(schedule.getDoctor().getUser().getFullName())
                                .scheduleDate(schedule.getScheduleDate())
                                .startTime(schedule.getWorkShift().getStartTime())
                                .endTime(schedule.getWorkShift().getEndTime())
                                .build();
        }

        // Phương thức hỗ trợ để chuyển đổi từ entity sang DTO
        private DoctorScheduleDTO mapToDoctorScheduleDTO(DoctorSchedule schedule) {
                DoctorScheduleDTO dto = new DoctorScheduleDTO();
                dto.setId(schedule.getId());
                dto.setDoctorId(schedule.getDoctor().getDoctorId());
                dto.setDoctorName(schedule.getDoctor().getUser().getFullName());
                dto.setScheduleDate(schedule.getScheduleDate());
                dto.setStartTime(schedule.getWorkShift().getStartTime());
                dto.setEndTime(schedule.getWorkShift().getEndTime());
                dto.setIsAvailable(true); // Default to true since we removed appointment slots

                return dto;
        }
}