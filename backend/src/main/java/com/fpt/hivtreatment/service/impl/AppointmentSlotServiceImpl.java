package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.dto.AppointmentSlotDTO;
import com.fpt.hivtreatment.model.entity.AppointmentSlot;
import com.fpt.hivtreatment.model.entity.DoctorProfile;
import com.fpt.hivtreatment.model.entity.DoctorSchedule;
import com.fpt.hivtreatment.model.entity.TimeSlot;
import com.fpt.hivtreatment.model.entity.WorkShift;
import com.fpt.hivtreatment.payload.request.GenerateAppointmentSlotsRequest;
import com.fpt.hivtreatment.repository.AppointmentSlotRepository;
import com.fpt.hivtreatment.repository.DoctorProfileRepository;
import com.fpt.hivtreatment.repository.DoctorScheduleRepository;
import com.fpt.hivtreatment.repository.TimeSlotRepository;
import com.fpt.hivtreatment.repository.WorkShiftRepository;
import com.fpt.hivtreatment.service.AppointmentSlotService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentSlotServiceImpl implements AppointmentSlotService {
        private static final Logger logger = LoggerFactory.getLogger(AppointmentSlotServiceImpl.class);

        private final AppointmentSlotRepository appointmentSlotRepository;
        private final DoctorScheduleRepository doctorScheduleRepository;
        private final DoctorProfileRepository doctorProfileRepository;
        private final TimeSlotRepository timeSlotRepository;
        private final WorkShiftRepository workShiftRepository;

        @Override
        @Transactional
        public List<AppointmentSlotDTO> generateAppointmentSlots(GenerateAppointmentSlotsRequest request) {
                // Lấy thông tin từ request
                Long doctorScheduleId = request.getDoctor_schedule_id();
                Long doctorId = request.getDoctor_id();
                String scheduleDateStr = request.getSchedule_date();
                Long workShiftId = request.getWork_shift_id();

                logger.info("Generating appointment slots for doctor schedule ID: {}, doctor ID: {}, date: {}, shift ID: {}",
                                doctorScheduleId, doctorId, scheduleDateStr, workShiftId);

                // Lấy lịch làm việc của bác sĩ
                DoctorSchedule doctorSchedule = doctorScheduleRepository.findById(doctorScheduleId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Không tìm thấy lịch làm việc với ID: " + doctorScheduleId));

                // Lấy thông tin bác sĩ
                DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + doctorId));

                // Lấy ca làm việc
                WorkShift workShift = workShiftRepository.findById(workShiftId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Không tìm thấy ca làm việc với ID: " + workShiftId));

                // Parse ngày từ chuỗi
                LocalDate scheduleDate = LocalDate.parse(scheduleDateStr);

                // Lấy thời gian bắt đầu và kết thúc của ca làm việc
                LocalTime shiftStartTime = workShift.getStartTime();
                LocalTime shiftEndTime = workShift.getEndTime();

                logger.info("Work shift time: {} - {}",
                                shiftStartTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                shiftEndTime.format(DateTimeFormatter.ofPattern("HH:mm")));

                // Xóa tất cả slot hiện có cho lịch bác sĩ này (nếu có)
                appointmentSlotRepository.deleteByDoctorScheduleId(doctorScheduleId);

                // Lấy tất cả time slots từ database
                List<TimeSlot> allTimeSlots = timeSlotRepository.findAll();
                logger.info("Found {} time slots in database", allTimeSlots.size());

                // Tạo mẫu time slots nếu không có
                if (allTimeSlots.isEmpty()) {
                        logger.warn("No time slots found in database. Creating default time slots...");
                        createDefaultTimeSlots();
                        allTimeSlots = timeSlotRepository.findAll();
                        logger.info("Created default time slots. Now have {} time slots in database",
                                        allTimeSlots.size());
                }

                // Lọc các time slots nằm trong khoảng thời gian của ca làm việc
                List<TimeSlot> validTimeSlots = allTimeSlots.stream()
                                .filter(timeSlot -> {
                                        // Lấy thời gian bắt đầu và kết thúc của slot
                                        LocalTime slotStartTime = timeSlot.getStartTime();
                                        LocalTime slotEndTime = timeSlot.getEndTime();

                                        // Kiểm tra slot có nằm trong ca làm việc không
                                        boolean isWithinWorkshift = !slotStartTime.isBefore(shiftStartTime)
                                                        && !slotStartTime.isAfter(shiftEndTime.minusMinutes(30));

                                        // Nếu có giờ nghỉ, kiểm tra slot có nằm trong giờ nghỉ không
                                        boolean isDuringBreak = false;
                                        if (workShift.getBreakStart() != null && workShift.getBreakEnd() != null) {
                                                LocalTime breakStart = workShift.getBreakStart();
                                                LocalTime breakEnd = workShift.getBreakEnd();

                                                logger.info("Kiểm tra slot {} - {} với giờ nghỉ {} - {}",
                                                                slotStartTime.format(
                                                                                DateTimeFormatter.ofPattern("HH:mm")),
                                                                slotEndTime.format(
                                                                                DateTimeFormatter.ofPattern("HH:mm")),
                                                                breakStart.format(DateTimeFormatter.ofPattern("HH:mm")),
                                                                breakEnd.format(DateTimeFormatter.ofPattern("HH:mm")));

                                                // Slot nằm trong giờ nghỉ nếu bắt đầu hoặc kết thúc trong giờ nghỉ
                                                isDuringBreak =
                                                                // Bắt đầu trong giờ nghỉ
                                                                (slotStartTime.compareTo(breakStart) >= 0
                                                                                && slotStartTime.compareTo(
                                                                                                breakEnd) < 0)
                                                                                ||
                                                // Kết thúc trong giờ nghỉ
                                                                                (slotEndTime.compareTo(breakStart) > 0
                                                                                                && slotEndTime.compareTo(
                                                                                                                breakEnd) <= 0)
                                                                                ||
                                                // Bao trọn giờ nghỉ
                                                                                (slotStartTime.compareTo(
                                                                                                breakStart) <= 0
                                                                                                && slotEndTime.compareTo(
                                                                                                                breakEnd) >= 0);

                                                if (isDuringBreak) {
                                                        logger.info("Loại bỏ slot {} - {} do nằm trong giờ nghỉ {} - {}",
                                                                        slotStartTime.format(DateTimeFormatter
                                                                                        .ofPattern("HH:mm")),
                                                                        slotEndTime.format(DateTimeFormatter
                                                                                        .ofPattern("HH:mm")),
                                                                        breakStart.format(DateTimeFormatter
                                                                                        .ofPattern("HH:mm")),
                                                                        breakEnd.format(DateTimeFormatter
                                                                                        .ofPattern("HH:mm")));
                                                }
                                        }

                                        // Slot hợp lệ khi nằm trong ca làm việc VÀ không nằm trong giờ nghỉ
                                        boolean isValid = isWithinWorkshift && !isDuringBreak;

                                        if (isValid) {
                                                logger.debug("Valid time slot found: {}",
                                                                slotStartTime.format(
                                                                                DateTimeFormatter.ofPattern("HH:mm")));
                                        }

                                        return isValid;
                                })
                                .collect(Collectors.toList());

                logger.info("Found {} valid time slots within work shift time range", validTimeSlots.size());

                if (validTimeSlots.isEmpty()) {
                        logger.warn("No valid time slots found for the given work shift. Please check time slot and work shift configuration.");

                        // Nếu không có time slots hợp lệ, trả về danh sách rỗng
                        return new ArrayList<>();
                }

                // Tạo các appointment slots mới
                List<AppointmentSlot> createdSlots = new ArrayList<>();
                for (TimeSlot timeSlot : validTimeSlots) {
                        AppointmentSlot slot = AppointmentSlot.builder()
                                        .doctorSchedule(doctorSchedule)
                                        .timeSlot(timeSlot)
                                        .doctor(doctor)
                                        .isAvailable(true)
                                        .build();

                        createdSlots.add(appointmentSlotRepository.save(slot));
                }

                logger.info("Created {} appointment slots for doctor schedule ID: {}", createdSlots.size(),
                                doctorScheduleId);

                // Chuyển đổi và trả về danh sách DTO
                return createdSlots.stream()
                                .map(this::mapToAppointmentSlotDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public List<AppointmentSlotDTO> getSlotsByScheduleId(Long doctorScheduleId) {
                List<AppointmentSlot> slots = appointmentSlotRepository.findByDoctorScheduleId(doctorScheduleId);
                return slots.stream()
                                .map(this::mapToAppointmentSlotDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public List<AppointmentSlotDTO> getSlotsByDoctorAndDate(Long doctorId, String dateStr) {
                logger.info("Getting appointment slots for doctor ID: {} on date: {}", doctorId, dateStr);

                try {
                        // Parse the date string to LocalDate
                        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);

                        // Find the doctor
                        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Doctor not found with ID: " + doctorId));

                        // Find slots by doctor and date
                        List<AppointmentSlot> slots = appointmentSlotRepository
                                        .findByDoctorAndDoctorSchedule_ScheduleDate(doctor, date);

                        logger.info("Found {} appointment slots for doctor ID: {} on date: {}",
                                        slots.size(), doctorId, dateStr);

                        // Convert to DTOs
                        return slots.stream()
                                        .map(this::mapToAppointmentSlotDTO)
                                        .collect(Collectors.toList());
                } catch (Exception e) {
                        logger.error("Error getting appointment slots by doctor and date", e);
                        return new ArrayList<>();
                }
        }

        /**
         * Tạo các time slots mặc định nếu chưa có
         */
        private void createDefaultTimeSlots() {
                logger.info("Creating default time slots");

                // Thời gian làm việc từ 8:00 đến 17:30, mỗi slot 30 phút
                LocalTime startOfDay = LocalTime.of(8, 0);
                LocalTime endOfDay = LocalTime.of(17, 0);

                List<TimeSlot> defaultSlots = new ArrayList<>();

                LocalTime currentTime = startOfDay;
                while (currentTime.isBefore(endOfDay)) {
                        LocalTime slotEndTime = currentTime.plusMinutes(30);

                        TimeSlot slot = TimeSlot.builder()
                                        .startTime(currentTime)
                                        .endTime(slotEndTime)
                                        .build();

                        defaultSlots.add(slot);
                        currentTime = slotEndTime;
                }

                logger.info("Saving {} default time slots", defaultSlots.size());
                timeSlotRepository.saveAll(defaultSlots);
        }

        /**
         * Chuyển đổi từ entity sang DTO
         */
        private AppointmentSlotDTO mapToAppointmentSlotDTO(AppointmentSlot slot) {
                // Get doctor name safely
                String doctorName = "Unknown Doctor";
                try {
                        if (slot.getDoctor() != null) {
                                // Try to get the doctor's name from wherever it's stored in your model
                                // This might be user.fullName, user.username, or a direct field on
                                // DoctorProfile
                                if (slot.getDoctor().getUser() != null) {
                                        doctorName = slot.getDoctor().getUser().getUsername();
                                }
                        }
                } catch (Exception e) {
                        logger.warn("Could not get doctor name: {}", e.getMessage());
                }

                return AppointmentSlotDTO.builder()
                                .id(slot.getId())
                                .doctorScheduleId(slot.getDoctorSchedule().getId())
                                .timeSlotId(slot.getTimeSlot().getId())
                                .doctorId(slot.getDoctor().getDoctorId())
                                .isAvailable(slot.getIsAvailable())
                                .createdAt(slot.getCreatedAt())
                                // Add additional fields for frontend
                                .timeSlotStart(slot.getTimeSlot().getStartTime().toString())
                                .timeSlotEnd(slot.getTimeSlot().getEndTime().toString())
                                .doctorName(doctorName)
                                .scheduleDate(slot.getDoctorSchedule().getScheduleDate().toString())
                                .build();
        }
}