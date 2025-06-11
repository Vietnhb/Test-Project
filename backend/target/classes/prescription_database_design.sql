-- ========================================
-- THIẾT KẾ LẠI DATABASE CHO LUỒNG KÊ ĐƠN THUỐC
-- Dựa trên phân tích workflow thực tế từ giao diện bác sĩ
-- ========================================

-- ========================================
-- 1. BẢNG ĐƠN THUỐC CHÍNH (PRESCRIPTION HEADER)
-- ========================================
-- Đại diện cho 1 lần kê đơn thuốc của bác sĩ
CREATE TABLE prescriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medical_record_id BIGINT NOT NULL,          -- Liên kết với lần khám bệnh
    patient_id BIGINT NOT NULL,                 -- Bệnh nhân được kê đơn
    doctor_id BIGINT NOT NULL,                  -- Bác sĩ kê đơn
    protocol_id BIGINT,                         -- Phác đồ điều trị được áp dụng
    
    -- Thông tin thời gian điều trị
    treatment_start_date DATE NOT NULL,         -- Ngày bắt đầu uống thuốc (từ giao diện)
    treatment_end_date DATE NOT NULL,           -- Ngày kết thúc uống thuốc (từ giao diện)
    treatment_duration_days INT GENERATED ALWAYS AS (DATEDIFF(treatment_end_date, treatment_start_date) + 1) STORED,
    
    -- Ghi chú của bác sĩ
    doctor_notes TEXT,                          -- Ghi chú đặc biệt cho bệnh nhân
    protocol_notes TEXT,                        -- Ghi chú về phác đồ
    
    -- Trạng thái đơn thuốc
    status VARCHAR(50) DEFAULT 'Đã kê',         -- 'Đã kê', 'Đã cấp phát', 'Hoàn thành'
    
    -- Thời gian tạo và cập nhật
    prescription_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys
    FOREIGN KEY (medical_record_id) REFERENCES medical_records(id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (protocol_id) REFERENCES treatment_protocols(id) ON DELETE SET NULL
);

-- ========================================
-- 2. BẢNG CHI TIẾT THUỐC TRONG ĐƠN (PRESCRIPTION ITEMS)
-- ========================================
-- Mỗi dòng đại diện cho 1 loại thuốc được kê trong đơn
CREATE TABLE prescription_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,            -- Thuộc đơn thuốc nào
    medication_id BIGINT NOT NULL,              -- Thuốc được kê
    
    -- Liều lượng theo từng buổi trong ngày (từ giao diện)
    morning_dose INT DEFAULT 0,                 -- Số viên buổi sáng
    noon_dose INT DEFAULT 0,                    -- Số viên buổi trưa
    afternoon_dose INT DEFAULT 0,               -- Số viên buổi chiều
    evening_dose INT DEFAULT 0,                 -- Số viên buổi tối
    
    -- Tính toán tự động
    daily_total INT GENERATED ALWAYS AS (morning_dose + noon_dose + afternoon_dose + evening_dose) STORED,
    
    -- Số lượng thuốc và đơn vị
    total_quantity INT,                         -- Tổng số viên thuốc (tự động tính: daily_total × duration)
    unit VARCHAR(20) DEFAULT 'viên',            -- Đơn vị: 'viên', 'ml', 'gói'
    
    -- Hướng dẫn sử dụng
    usage_instructions TEXT,                    -- Hướng dẫn uống thuốc (trước/sau ăn, etc)
    special_notes TEXT,                         -- Ghi chú đặc biệt cho thuốc này
    
    -- Thời gian
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys và constraints
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE,
    FOREIGN KEY (medication_id) REFERENCES medications(id) ON DELETE CASCADE,
    
    -- Đảm bảo không trùng thuốc trong cùng 1 đơn
    UNIQUE KEY unique_prescription_medication (prescription_id, medication_id),
    
    -- Ít nhất phải có 1 buổi được kê thuốc
    CONSTRAINT check_at_least_one_dose CHECK (morning_dose > 0 OR noon_dose > 0 OR afternoon_dose > 0 OR evening_dose > 0)
);

-- ========================================
-- 3. BẢNG LỊCH NHẮC UỐNG THUỐC (MEDICATION REMINDERS)
-- ========================================
-- Tự động tạo lịch nhắc dựa trên prescription_items
CREATE TABLE medication_reminders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_item_id BIGINT NOT NULL,       -- Thuốc nào được nhắc
    patient_id BIGINT NOT NULL,                 -- Bệnh nhân cần nhắc
    
    -- Thông tin nhắc
    reminder_type ENUM('MORNING', 'NOON', 'AFTERNOON', 'EVENING') NOT NULL,
    reminder_time TIME NOT NULL,                -- Giờ nhắc (8:00, 12:00, 17:00, 20:00)
    dose_amount INT NOT NULL,                   -- Số viên cần uống vào thời điểm này
    
    -- Thời gian nhắc
    start_date DATE NOT NULL,                   -- Ngày bắt đầu nhắc
    end_date DATE NOT NULL,                     -- Ngày kết thúc nhắc
    
    -- Trạng thái
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign keys
    FOREIGN KEY (prescription_item_id) REFERENCES prescription_items(id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Index cho performance
    INDEX idx_patient_date_time (patient_id, start_date, reminder_time),
    INDEX idx_active_reminders (is_active, start_date, end_date)
);

-- ========================================
-- 4. BẢNG LỊCH SỬ TUÂN THỦ UỐNG THUỐC (MEDICATION ADHERENCE LOG)
-- ========================================
-- Theo dõi việc bệnh nhân có uống thuốc đúng giờ không
CREATE TABLE medication_adherence_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_item_id BIGINT NOT NULL,       -- Thuốc nào
    patient_id BIGINT NOT NULL,                 -- Bệnh nhân nào
    
    -- Thông tin về lần uống thuốc
    scheduled_date DATE NOT NULL,               -- Ngày theo lịch
    scheduled_time_type ENUM('MORNING', 'NOON', 'AFTERNOON', 'EVENING') NOT NULL,
    scheduled_dose INT NOT NULL,                -- Số viên theo lịch
    
    -- Thực tế
    actual_taken_time TIMESTAMP,                -- Thời gian thực tế uống (null = chưa uống)
    actual_dose_taken INT DEFAULT 0,            -- Số viên thực tế uống
    
    -- Trạng thái tuân thủ
    adherence_status ENUM('TAKEN_ON_TIME', 'TAKEN_LATE', 'MISSED', 'PARTIAL') DEFAULT 'MISSED',
    
    -- Ghi chú
    patient_notes TEXT,                         -- Ghi chú của bệnh nhân (lý do bỏ liều, etc)
    
    -- Thời gian
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys
    FOREIGN KEY (prescription_item_id) REFERENCES prescription_items(id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Index cho báo cáo tuân thủ
    INDEX idx_patient_adherence (patient_id, scheduled_date),
    INDEX idx_adherence_status (adherence_status, scheduled_date),
    
    -- Unique constraint
    UNIQUE KEY unique_dose_schedule (prescription_item_id, scheduled_date, scheduled_time_type)
);

-- ========================================
-- 5. VIEW HỖ TRỢ BÁO CÁO VÀ TRUY VẤN
-- ========================================

-- View tóm tắt đơn thuốc cho bác sĩ
CREATE VIEW v_prescription_summary AS
SELECT 
    p.id as prescription_id,
    p.prescription_date,
    CONCAT(pt.full_name) as patient_name,
    CONCAT(dr.full_name) as doctor_name,
    tp.name as protocol_name,
    p.treatment_start_date,
    p.treatment_end_date,
    p.treatment_duration_days,
    COUNT(pi.id) as total_medications,
    SUM(pi.daily_total) as total_daily_pills,
    p.status,
    p.doctor_notes
FROM prescriptions p
JOIN users pt ON p.patient_id = pt.id
JOIN users dr ON p.doctor_id = dr.id
LEFT JOIN treatment_protocols tp ON p.protocol_id = tp.id
LEFT JOIN prescription_items pi ON p.id = pi.prescription_id
GROUP BY p.id;

-- View chi tiết đơn thuốc cho in ấn
CREATE VIEW v_prescription_details AS
SELECT 
    p.id as prescription_id,
    p.prescription_date,
    p.treatment_start_date,
    p.treatment_end_date,
    p.treatment_duration_days,
    p.doctor_notes,
    -- Thông tin bệnh nhân
    pt.full_name as patient_name,
    pt.phone as patient_phone,
    -- Thông tin bác sĩ
    dr.full_name as doctor_name,
    -- Thông tin phác đồ
    tp.name as protocol_name,
    tp.description as protocol_description,
    -- Thông tin thuốc
    pi.id as item_id,
    m.name as medication_name,
    m.generic_name,
    m.strength,
    m.dosage_form,
    pi.morning_dose,
    pi.noon_dose,
    pi.afternoon_dose,
    pi.evening_dose,
    pi.daily_total,
    pi.total_quantity,
    pi.unit,
    pi.usage_instructions,
    pi.special_notes
FROM prescriptions p
JOIN users pt ON p.patient_id = pt.id
JOIN users dr ON p.doctor_id = dr.id
LEFT JOIN treatment_protocols tp ON p.protocol_id = tp.id
JOIN prescription_items pi ON p.id = pi.prescription_id
JOIN medications m ON pi.medication_id = m.id
ORDER BY p.id, pi.id;

-- View tuân thủ uống thuốc
CREATE VIEW v_medication_adherence AS
SELECT 
    mal.patient_id,
    pt.full_name as patient_name,
    m.name as medication_name,
    mal.scheduled_date,
    mal.scheduled_time_type,
    mal.scheduled_dose,
    mal.actual_dose_taken,
    mal.adherence_status,
    CASE 
        WHEN mal.adherence_status = 'TAKEN_ON_TIME' THEN 100
        WHEN mal.adherence_status = 'TAKEN_LATE' THEN 80
        WHEN mal.adherence_status = 'PARTIAL' THEN 50
        ELSE 0
    END as adherence_score
FROM medication_adherence_log mal
JOIN prescription_items pi ON mal.prescription_item_id = pi.id
JOIN medications m ON pi.medication_id = m.id
JOIN users pt ON mal.patient_id = pt.id;

-- ========================================
-- 6. STORED PROCEDURES HỖ TRỢ
-- ========================================

-- Procedure tự động tạo lịch nhắc khi có đơn thuốc mới
DELIMITER //
CREATE PROCEDURE sp_create_medication_reminders(IN p_prescription_id BIGINT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_item_id BIGINT;
    DECLARE v_patient_id BIGINT;
    DECLARE v_morning_dose, v_noon_dose, v_afternoon_dose, v_evening_dose INT;
    DECLARE v_start_date, v_end_date DATE;
    
    -- Cursor để lấy tất cả prescription items
    DECLARE item_cursor CURSOR FOR
        SELECT pi.id, p.patient_id, pi.morning_dose, pi.noon_dose, 
               pi.afternoon_dose, pi.evening_dose, p.treatment_start_date, p.treatment_end_date
        FROM prescription_items pi
        JOIN prescriptions p ON pi.prescription_id = p.id
        WHERE pi.prescription_id = p_prescription_id;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN item_cursor;
    
    read_loop: LOOP
        FETCH item_cursor INTO v_item_id, v_patient_id, v_morning_dose, v_noon_dose, 
                              v_afternoon_dose, v_evening_dose, v_start_date, v_end_date;
        
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Tạo reminder cho buổi sáng
        IF v_morning_dose > 0 THEN
            INSERT INTO medication_reminders (prescription_item_id, patient_id, reminder_type, 
                                            reminder_time, dose_amount, start_date, end_date)
            VALUES (v_item_id, v_patient_id, 'MORNING', '08:00:00', v_morning_dose, v_start_date, v_end_date);
        END IF;
        
        -- Tạo reminder cho buổi trưa
        IF v_noon_dose > 0 THEN
            INSERT INTO medication_reminders (prescription_item_id, patient_id, reminder_type, 
                                            reminder_time, dose_amount, start_date, end_date)
            VALUES (v_item_id, v_patient_id, 'NOON', '12:00:00', v_noon_dose, v_start_date, v_end_date);
        END IF;
        
        -- Tạo reminder cho buổi chiều
        IF v_afternoon_dose > 0 THEN
            INSERT INTO medication_reminders (prescription_item_id, patient_id, reminder_type, 
                                            reminder_time, dose_amount, start_date, end_date)
            VALUES (v_item_id, v_patient_id, 'AFTERNOON', '17:00:00', v_afternoon_dose, v_start_date, v_end_date);
        END IF;
        
        -- Tạo reminder cho buổi tối
        IF v_evening_dose > 0 THEN
            INSERT INTO medication_reminders (prescription_item_id, patient_id, reminder_type, 
                                            reminder_time, dose_amount, start_date, end_date)
            VALUES (v_item_id, v_patient_id, 'EVENING', '20:00:00', v_evening_dose, v_start_date, v_end_date);
        END IF;
        
    END LOOP;
    
    CLOSE item_cursor;
END //
DELIMITER ;

-- Procedure tạo lịch tuân thủ cho bệnh nhân
DELIMITER //
CREATE PROCEDURE sp_generate_adherence_schedule(IN p_prescription_id BIGINT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_current_date DATE;
    DECLARE v_item_id BIGINT;
    DECLARE v_patient_id BIGINT;
    DECLARE v_morning_dose, v_noon_dose, v_afternoon_dose, v_evening_dose INT;
    DECLARE v_start_date, v_end_date DATE;
    
    -- Cursor để lấy tất cả prescription items
    DECLARE item_cursor CURSOR FOR
        SELECT pi.id, p.patient_id, pi.morning_dose, pi.noon_dose, 
               pi.afternoon_dose, pi.evening_dose, p.treatment_start_date, p.treatment_end_date
        FROM prescription_items pi
        JOIN prescriptions p ON pi.prescription_id = p.id
        WHERE pi.prescription_id = p_prescription_id;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN item_cursor;
    
    item_loop: LOOP
        FETCH item_cursor INTO v_item_id, v_patient_id, v_morning_dose, v_noon_dose, 
                              v_afternoon_dose, v_evening_dose, v_start_date, v_end_date;
        
        IF done THEN
            LEAVE item_loop;
        END IF;
        
        SET v_current_date = v_start_date;
        
        -- Tạo lịch cho từng ngày
        date_loop: WHILE v_current_date <= v_end_date DO
            -- Tạo record cho buổi sáng
            IF v_morning_dose > 0 THEN
                INSERT INTO medication_adherence_log (prescription_item_id, patient_id, 
                                                    scheduled_date, scheduled_time_type, scheduled_dose)
                VALUES (v_item_id, v_patient_id, v_current_date, 'MORNING', v_morning_dose);
            END IF;
            
            -- Tạo record cho buổi trưa
            IF v_noon_dose > 0 THEN
                INSERT INTO medication_adherence_log (prescription_item_id, patient_id, 
                                                    scheduled_date, scheduled_time_type, scheduled_dose)
                VALUES (v_item_id, v_patient_id, v_current_date, 'NOON', v_noon_dose);
            END IF;
            
            -- Tạo record cho buổi chiều  
            IF v_afternoon_dose > 0 THEN
                INSERT INTO medication_adherence_log (prescription_item_id, patient_id, 
                                                    scheduled_date, scheduled_time_type, scheduled_dose)
                VALUES (v_item_id, v_patient_id, v_current_date, 'AFTERNOON', v_afternoon_dose);
            END IF;
            
            -- Tạo record cho buổi tối
            IF v_evening_dose > 0 THEN
                INSERT INTO medication_adherence_log (prescription_item_id, patient_id, 
                                                    scheduled_date, scheduled_time_type, scheduled_dose)
                VALUES (v_item_id, v_patient_id, v_current_date, 'EVENING', v_evening_dose);
            END IF;
            
            SET v_current_date = DATE_ADD(v_current_date, INTERVAL 1 DAY);
        END WHILE date_loop;
        
    END LOOP item_loop;
    
    CLOSE item_cursor;
END //
DELIMITER ;

-- ========================================
-- 7. TRIGGERS TỰ ĐỘNG
-- ========================================

-- Trigger tự động tính số lượng thuốc khi thêm/sửa prescription_item
DELIMITER //
CREATE TRIGGER tr_calculate_total_quantity
    BEFORE INSERT ON prescription_items
    FOR EACH ROW
BEGIN
    DECLARE v_duration INT;
    
    -- Lấy số ngày điều trị từ prescription
    SELECT treatment_duration_days INTO v_duration
    FROM prescriptions 
    WHERE id = NEW.prescription_id;
    
    -- Tính tổng số viên cần
    SET NEW.total_quantity = (NEW.morning_dose + NEW.noon_dose + NEW.afternoon_dose + NEW.evening_dose) * v_duration;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER tr_update_total_quantity
    BEFORE UPDATE ON prescription_items
    FOR EACH ROW
BEGIN
    DECLARE v_duration INT;
    
    -- Lấy số ngày điều trị từ prescription
    SELECT treatment_duration_days INTO v_duration
    FROM prescriptions 
    WHERE id = NEW.prescription_id;
    
    -- Tính lại tổng số viên cần
    SET NEW.total_quantity = (NEW.morning_dose + NEW.noon_dose + NEW.afternoon_dose + NEW.evening_dose) * v_duration;
END //
DELIMITER ;

-- Trigger tự động tạo lịch nhắc khi có đơn thuốc mới
DELIMITER //
CREATE TRIGGER tr_create_reminders_after_prescription
    AFTER INSERT ON prescriptions
    FOR EACH ROW
BEGIN
    -- Gọi procedure tạo lịch nhắc
    CALL sp_create_medication_reminders(NEW.id);
    
    -- Gọi procedure tạo lịch tuân thủ
    CALL sp_generate_adherence_schedule(NEW.id);
END //
DELIMITER ;

-- ========================================
-- 8. DỮ LIỆU MẪU CHO TEST
-- ========================================

-- Test tạo đơn thuốc mẫu
INSERT INTO prescriptions (medical_record_id, patient_id, doctor_id, protocol_id, 
                          treatment_start_date, treatment_end_date, doctor_notes) 
VALUES (1, 2, 3, 2, '2025-06-11', '2025-07-11', 'Uống thuốc đúng giờ, theo dõi tác dụng phụ');

-- Test thêm thuốc vào đơn
INSERT INTO prescription_items (prescription_id, medication_id, morning_dose, noon_dose, 
                               afternoon_dose, evening_dose, usage_instructions) 
VALUES 
(1, 1, 1, 0, 0, 1, 'Uống sau ăn, tránh uống cùng sữa'),
(1, 5, 1, 0, 0, 1, 'Uống cùng với Efavirenz'),
(1, 7, 1, 0, 0, 0, 'Uống buổi sáng, nhiều nước');
