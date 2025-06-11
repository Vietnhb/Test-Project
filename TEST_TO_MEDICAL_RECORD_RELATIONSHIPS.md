# LUỒNG KHÁM BỆNH & XÉT NGHIỆM - MỐI QUAN HỆ DATABASE

## 🔄 LUỒNG HOẠT ĐỘNG THỰC TẾ

### 1. LUỒNG KHÁM BỆNH VÀ CHỈ ĐỊNH XÉT NGHIỆM
```
BỆNH NHÂN ĐẾN KHÁM 
    ↓
BÁC SĨ TẠO MEDICAL RECORD (ghi chú khám lâm sàng)
    ↓
BÁC SĨ CHỈ ĐỊNH XÉT NGHIỆM (liên kết với medical record)
    ↓
THANH TOÁN XÉT NGHIỆM (VNPAY)
    ↓
THỰC HIỆN XÉT NGHIỆM
    ↓
KẾT QUẢ XÉT NGHIỆM
    ↓
CẬP NHẬT VÀO MEDICAL RECORD (kết luận chẩn đoán)
```

### 2. MỐI QUAN HỆ GIỮA CÁC BẢNG

#### 🔗 **LUỒNG THỰC TẾ: Medical Record → Test → Results**
```sql
users (patient) 
    ↓ 1:N
medical_records (hồ sơ bệnh án - tạo trước)
    ↓ 1:N (bác sĩ chỉ định từ medical record)
lab_test_orders (đơn xét nghiệm)
    ↓ 1:1
lab_test_results (kết quả XN)
    ↓ N:M (cập nhật lại vào medical record)
medical_record_lab_results (liên kết)
    ↓ N:1
medical_records (cập nhật chẩn đoán cuối cùng)
```

## 📊 CHI TIẾT CÁC BẢNG VÀ MỐI QUAN HỆ

### 1. **BẢNG `users`** (Bệnh nhân)
- **Vai trò**: Lưu thông tin cơ bản của bệnh nhân
- **Khóa chính**: `id`
- **Quan hệ**: 1 bệnh nhân có nhiều đơn xét nghiệm

### 2. **BẢNG `test_types`** (Loại xét nghiệm)
- **Vai trò**: Danh mục các loại xét nghiệm (HIV, CD4, Viral Load, HBV, HCV)
- **Khóa chính**: `id`
- **Quan hệ**: 1 loại XN có nhiều đơn XN

### 3. **BẢNG `lab_test_orders`** (Đơn xét nghiệm)
- **Vai trò**: Lưu thông tin đơn đặt xét nghiệm được chỉ định từ medical record
- **Khóa chính**: `id`
- **Khóa ngoại**:
  - `test_type_id` → `test_types(id)`
  - `patient_id` → `users(id)`
  - `doctor_id` → `users(id)`
  - `medical_record_id` → `medical_records(id)` ⭐ **TRƯỜNG MỚI**
  - `staff_id` → `users(id)`
- **Quan hệ**: 
  - N:1 với `users` (bệnh nhân)
  - N:1 với `test_types`
  - N:1 với `medical_records` ⭐ **MỐI QUAN HỆ MỚI**
  - 1:1 với `lab_test_results`
  - 1:N với `payments`

### 4. **BẢNG `payments`** (Thanh toán)
- **Vai trò**: Quản lý thanh toán cho xét nghiệm (tích hợp VNPAY)
- **Khóa chính**: `id`
- **Khóa ngoại**: `lab_test_order_id` → `lab_test_orders(id)`
- **Quan hệ**: N:1 với `lab_test_orders`

### 5. **BẢNG `lab_test_results`** (Kết quả xét nghiệm)
- **Vai trò**: Lưu kết quả xét nghiệm
- **Khóa chính**: `id`
- **Khóa ngoại**: `lab_test_order_id` → `lab_test_orders(id)` (UNIQUE)
- **Quan hệ**: 
  - 1:1 với `lab_test_orders`
  - N:M với `medical_records` (qua bảng liên kết)

### 6. **BẢNG `medical_record_lab_results`** (Bảng liên kết)
- **Vai trò**: Liên kết kết quả XN với hồ sơ bệnh án
- **Khóa chính**: `id`
- **Khóa ngoại**:
  - `medical_record_id` → `medical_records(id)`
  - `lab_test_result_id` → `lab_test_results(id)`
- **Quan hệ**: Bảng trung gian N:M
- **Thông tin bổ sung**:
  - `result_interpretation`: Giải thích kết quả từ bác sĩ
  - `clinical_significance`: Ý nghĩa lâm sàng

### 7. **BẢNG `medical_records`** (Hồ sơ bệnh án)
- **Vai trò**: Lưu thông tin khám lâm sàng chi tiết
- **Khóa chính**: `id`
- **Khóa ngoại**:
  - `patient_id` → `users(id)`
  - `doctor_id` → `users(id)`
- **Quan hệ**: 
  - N:1 với `users` (bệnh nhân)
  - N:1 với `users` (bác sĩ)
  - N:M với `lab_test_results` (qua bảng liên kết)

## 🔄 LUỒNG DỮ LIỆU CHI TIẾT

### BƯỚC 1: Bệnh nhân đến khám - Bác sĩ tạo Medical Record
```sql
-- Bác sĩ tạo hồ sơ bệnh án ngay khi bệnh nhân đến khám
INSERT INTO medical_records (patient_id, doctor_id, visit_date, visit_type, 
    chief_complaint, symptoms, clinical_assessment)
VALUES (5, 2, '2024-01-20', 'Tư vấn', 
    'Lo lắng về tình trạng sức khỏe sau hành vi nguy cơ cao',
    'Không có triệu chứng cụ thể, lo lắng về khả năng nhiễm HIV',
    'Bệnh nhân cần tư vấn và sàng lọc HIV');
-- ✅ Medical Record ID = 3 được tạo
```

### BƯỚC 2: Bác sĩ chỉ định xét nghiệm từ Medical Record
```sql
-- Bác sĩ chỉ định xét nghiệm dựa trên medical record vừa tạo
INSERT INTO lab_test_orders (test_type_id, patient_id, doctor_id, medical_record_id, order_date, status)
VALUES (3, 5, 2, 3, '2024-01-20', 'Chờ thanh toán');
-- ✅ Lab Test Order được liên kết với Medical Record ID = 3
```

### BƯỚC 3: Thanh toán xét nghiệm (VNPAY)
```sql
INSERT INTO payments (lab_test_order_id, amount, payment_method, status)
VALUES (1, 500000, 'VNPAY', 'Đã thanh toán');

-- Cập nhật trạng thái đơn XN
UPDATE lab_test_orders SET status = 'Đã thanh toán' WHERE id = 1;
```

### BƯỚC 4: Thực hiện xét nghiệm và nhập kết quả
```sql
INSERT INTO lab_test_results (lab_test_order_id, result_data, result_summary)
VALUES (1, '{"HIV": "Positive", "CD4": "180 cells/µL"}', 'HIV dương tính, CD4 thấp');
```

### BƯỚC 5: Liên kết kết quả XN với Medical Record
```sql
-- Kết quả XN được liên kết ngược lại với Medical Record
INSERT INTO medical_record_lab_results (medical_record_id, lab_test_result_id, result_interpretation)
VALUES (3, 1, 'Kết quả xác nhận HIV dương tính, cần bắt đầu điều trị ARV');
```

### BƯỚC 6: Bác sĩ cập nhật Medical Record với chẩn đoán cuối cùng
```sql
-- Cập nhật Medical Record với chẩn đoán dựa trên kết quả XN
UPDATE medical_records 
SET diagnosis = 'HIV dương tính, CD4: 180 cells/µL', 
    treatment_plan = 'Bắt đầu điều trị ARV ngay lập tức',
    differential_diagnosis = 'Đã loại trừ các nhiễm trùng cơ hội'
WHERE id = 3;
```

## 📈 CÁC QUERY QUAN TRỌNG

### 1. Lấy tất cả kết quả XN của bệnh nhân trong hồ sơ bệnh án
```sql
SELECT 
    mr.id as medical_record_id,
    mr.visit_date,
    mr.diagnosis,
    ltr.result_data,
    ltr.result_summary,
    mrlr.result_interpretation,
    tt.name as test_name
FROM medical_records mr
JOIN medical_record_lab_results mrlr ON mr.id = mrlr.medical_record_id
JOIN lab_test_results ltr ON mrlr.lab_test_result_id = ltr.id
JOIN lab_test_orders lto ON ltr.lab_test_order_id = lto.id
JOIN test_types tt ON lto.test_type_id = tt.id
WHERE mr.patient_id = 3
ORDER BY mr.visit_date DESC;
```

### 2. Theo dõi luồng thanh toán của xét nghiệm
```sql
SELECT 
    lto.id as order_id,
    lto.order_date,
    tt.name as test_name,
    p.amount,
    p.payment_method,
    p.status as payment_status,
    lto.status as order_status
FROM lab_test_orders lto
JOIN test_types tt ON lto.test_type_id = tt.id
LEFT JOIN payments p ON lto.id = p.lab_test_order_id
WHERE lto.patient_id = 3;
```

### 3. Thống kê xét nghiệm theo trạng thái
```sql
SELECT 
    lto.status,
    COUNT(*) as total_orders,
    SUM(CASE WHEN p.status = 'Đã thanh toán' THEN p.amount ELSE 0 END) as total_revenue
FROM lab_test_orders lto
LEFT JOIN payments p ON lto.id = p.lab_test_order_id
GROUP BY lto.status;
```

## 🔔 HỆ THỐNG THÔNG BÁO

### Thông báo kết quả xét nghiệm
```sql
INSERT INTO notifications (user_id, notification_type, title, content, lab_test_result_id)
VALUES (3, 'LAB_RESULT', 'Kết quả xét nghiệm HIV', 'Kết quả xét nghiệm của bạn đã có...', 1);
```

## 🎯 TÍCH HỢP VỚI HỆ THỐNG HIỆN CÓ

### 1. **Tích hợp VNPAY**
- Bảng `payments` lưu thông tin giao dịch VNPAY
- Các trường: `transaction_id`, `transaction_ref`, `bank_code`, `secure_hash`

### 2. **Tích hợp Email/SMS Reminders**
- Bảng `notifications` quản lý thông báo
- Bảng `email_templates` cho template email

### 3. **Quản lý đơn thuốc**
- Bảng `prescriptions` liên kết với `medical_records`
- Theo dõi việc cấp phát thuốc ARV

## ⚠️ LƯU Ý QUAN TRỌNG

1. **Tính toàn vẹn dữ liệu**: Mối quan hệ 1:1 giữa `lab_test_orders` và `lab_test_results`
2. **Bảo mật**: Thông tin y tế nhạy cảm cần mã hóa
3. **Audit Trail**: Các bảng có `created_at`, `updated_at` để theo dõi
4. **Cascade Delete**: Khi xóa bệnh nhân, tất cả dữ liệu liên quan sẽ bị xóa
5. **Backup**: Cần backup thường xuyên cho dữ liệu y tế

## 📋 CHECKLIST TRIỂN KHAI

- [x] Thiết kế mối quan hệ database
- [x] Tích hợp VNPAY payment gateway
- [x] Hệ thống thông báo tự động
- [x] Template email nhắc nhở
- [x] Quản lý đơn thuốc đơn giản
- [ ] API endpoints cho mobile app
- [ ] Báo cáo thống kê
- [ ] Backup và khôi phục dữ liệu
