# TÍCH HỢP MEDICATION VÀ TREATMENT PROTOCOL VÀO MEDICAL RECORD WORKFLOW

## TỔNG QUAN VẤN ĐỀ ĐÃ GIẢI QUYẾT

Trước đây, hệ thống có vấn đề về luồng tích hợp giữa:
- **Medical Records** (hồ sơ bệnh án)
- **Treatment Protocols** (phác đồ điều trị)  
- **Prescriptions** (đơn thuốc)
- **Medications** (thuốc)

**Vấn đề cũ:**
- `medical_records` chỉ có trường `treatment_plan` dạng TEXT tự do
- Không có liên kết trực tiếp với `treatment_protocols` table
- Bác sĩ phải tự viết kế hoạch điều trị thay vì chọn từ phác đồ chuẩn
- Khó theo dõi tuân thủ phác đồ điều trị chuẩn

## CÁC CẢI TIẾN ĐÃ THỰC HIỆN

### 1. ENHANCEMENT MEDICAL_RECORDS TABLE

**Thêm các trường mới:**
```sql
-- Liên kết với phác đồ điều trị
primary_protocol_id BIGINT,                 -- Phác đồ điều trị chính được áp dụng
secondary_protocol_id BIGINT,               -- Phác đồ điều trị phụ (dự phòng nhiễm trùng cơ hội)
protocol_start_date DATE,                   -- Ngày bắt đầu áp dụng phác đồ

-- Foreign key constraints
FOREIGN KEY (primary_protocol_id) REFERENCES treatment_protocols(id) ON DELETE SET NULL,
FOREIGN KEY (secondary_protocol_id) REFERENCES treatment_protocols(id) ON DELETE SET NULL
```

**Giữ lại trường cũ:**
- `treatment_plan TEXT` - để ghi chú tự do bổ sung ngoài phác đồ chuẩn

### 2. CẢI TIẾN TREATMENT_PROTOCOLS TABLE

**Thêm trường:**
```sql
created_by_doctor_id BIGINT,                -- Bác sĩ tạo phác đồ
FOREIGN KEY (created_by_doctor_id) REFERENCES users(id) ON DELETE SET NULL
```

### 3. CẬP NHẬT DỮ LIỆU MẪU (THEO LUỒNG Y TẾ ĐÚNG)

**Medical Records theo luồng thực tế:**
- **Medical Record ID 1**: Lần khám sàng lọc
  - `primary_protocol_id = NULL` (chưa có kết quả XN)
  - `secondary_protocol_id = NULL` (chưa có kết quả XN)  
  - `diagnosis = "Sàng lọc HIV và STI"`
  
- **Medical Record ID 2**: Lần tái khám với kết quả XN
  - `primary_protocol_id = 1` (Phác đồ ARV lần đầu - sau khi có kết quả)
  - `secondary_protocol_id = 3` (Dự phòng nhiễm trùng cơ hội - vì CD4 <200)
  - `protocol_start_date = '2024-01-15'` (ngày bắt đầu điều trị)
  - `diagnosis = "HIV-1 dương tính, CD4: 180 cells/µL"`

## LUỒNG HOẠT ĐỘNG MỚI (CẬP NHẬT THEO Y TẾ THỰC TẾ)

### A. GIAI ĐOẠN 1: KHÁM VÀ CHỈ ĐỊNH XÉT NGHIỆM

```
1. Bệnh nhân đến khám
   ↓
2. Tạo Medical Record (Lần 1 - Sàng lọc):
   - Thông tin khám lâm sàng chi tiết
   - Chẩn đoán sơ bộ: "Cần sàng lọc HIV"
   - primary_protocol_id = NULL (chưa thể chọn)
   - secondary_protocol_id = NULL (chưa thể chọn)
   - treatment_plan = "Chờ kết quả XN để quyết định phác đồ"
   ↓
3. Chỉ định xét nghiệm từ Medical Record:
   - HIV sàng lọc → Nếu (+) → HIV khẳng định
   - Nếu HIV (+) → CD4, Viral Load, chức năng gan/thận
   ↓
4. Thanh toán xét nghiệm (VNPAY)
   ↓
5. Thực hiện xét nghiệm và có kết quả
```

### B. GIAI ĐOẠN 2: TÁI KHÁM VÀ ĐIỀU TRỊ DỰA TRÊN KẾT QUẢ

```
1. Bệnh nhân tái khám với kết quả
   ↓
2. Cập nhật Medical Record (Lần 2 - Điều trị):
   - Xem xét kết quả xét nghiệm
   - Chẩn đoán chính thức dựa trên kết quả
   - Chọn Primary Protocol dựa trên CD4, Viral Load
   - Chọn Secondary Protocol nếu cần (VD: CD4 <200)
   - protocol_start_date = ngày bắt đầu điều trị
   ↓
3. Kê đơn thuốc theo Protocol đã chọn:
   - medical_record_id (từ lần tái khám)
   - protocol_id (đã chọn dựa trên kết quả XN)
   - medication_id (từ protocol)
   - Liều lượng theo protocol hoặc điều chỉnh
   ↓
4. Cấp phát thuốc và hướng dẫn bệnh nhân
   ↓
5. Lên lịch theo dõi theo monitoring_schedule của protocol
```

### C. BUSINESS LOGIC RULES (QUAN TRỌNG)

```
1. RULE: Protocol chỉ được chọn khi có kết quả xét nghiệm
   IF medical_record.diagnosis CONTAINS 'HIV dương tính' 
      AND lab_results.cd4_count IS NOT NULL
      AND lab_results.viral_load IS NOT NULL
   THEN allow protocol selection
   ELSE primary_protocol_id = NULL

2. RULE: Protocol selection dựa trên kết quả XN
   IF cd4_count < 200 
   THEN secondary_protocol_id = 3 (dự phòng nhiễm trùng cơ hội)

   IF viral_load > 1000 AND treatment_naive = TRUE
   THEN primary_protocol_id = 1 (ARV lần đầu)

   IF treatment_failure = TRUE AND drug_resistance = TRUE  
   THEN primary_protocol_id = 2 (ARV kháng thuốc)

3. RULE: Không kê đơn thuốc ARV nếu chưa có chẩn đoán HIV xác định
   Chỉ cho phép prescription khi medical_record có protocol_id
```

## CẤU TRÚC RELATIONSHIPS MỚI

### MEDICAL RECORD ↔ TREATMENT PROTOCOL
```sql
-- Medical Record có thể có nhiều protocols
medical_records.primary_protocol_id → treatment_protocols.id
medical_records.secondary_protocol_id → treatment_protocols.id

-- Treatment Protocol có thể được áp dụng cho nhiều medical records
1 protocol : many medical_records
```

### TREATMENT PROTOCOL ↔ MEDICATIONS
```sql
-- Protocol chứa nhiều thuốc (qua protocol_medications)
treatment_protocols.id ← protocol_medications.protocol_id
protocol_medications.medication_id → medications.id

-- Many-to-Many relationship with additional fields:
-- dosage, frequency, timing, duration_days, special_instructions
```

### MEDICAL RECORD ↔ PRESCRIPTIONS
```sql
-- Medical Record có thể có nhiều prescriptions
medical_records.id ← prescriptions.medical_record_id

-- Prescription liên kết với protocol (nếu kê theo phác đồ)
prescriptions.protocol_id → treatment_protocols.id
```

## SAMPLE QUERY PATTERNS

### 1. Lấy thông tin đầy đủ một lần khám
```sql
SELECT 
    mr.*,
    pp.name as primary_protocol_name,
    sp.name as secondary_protocol_name,
    pp.monitoring_schedule,
    pp.lab_tests_required
FROM medical_records mr
LEFT JOIN treatment_protocols pp ON mr.primary_protocol_id = pp.id
LEFT JOIN treatment_protocols sp ON mr.secondary_protocol_id = sp.id
WHERE mr.id = ?;
```

### 2. Lấy danh sách thuốc theo phác đồ
```sql
SELECT 
    m.name as medication_name,
    pm.dosage, pm.frequency, pm.timing,
    pm.special_instructions
FROM protocol_medications pm
JOIN medications m ON pm.medication_id = m.id
WHERE pm.protocol_id = ?
ORDER BY pm.display_order;
```

### 3. Theo dõi tuân thủ điều trị của bệnh nhân
```sql
SELECT 
    mr.visit_date,
    mr.diagnosis,
    tp.name as protocol_name,
    pr.medication_id,
    pr.is_dispensed,
    pr.next_refill_date
FROM medical_records mr
JOIN prescriptions pr ON mr.id = pr.medical_record_id
LEFT JOIN treatment_protocols tp ON pr.protocol_id = tp.id
WHERE mr.patient_id = ?
ORDER BY mr.visit_date DESC;
```

## LỢI ÍCH CỦA HỆ THỐNG MỚI

### 1. **Chuẩn hóa phác đồ điều trị**
- Bác sĩ chọn từ các phác đồ chuẩn đã được thiết lập
- Giảm sai sót trong kê đơn thuốc
- Đảm bảo tuân thủ guideline điều trị

### 2. **Theo dõi tự động**
- Hệ thống biết lịch theo dõi của từng phác đồ
- Tự động nhắc nhở tái khám và xét nghiệm
- Tracking adherence của bệnh nhân

### 3. **Linh hoạt trong điều trị**
- Vẫn cho phép ghi chú tự do trong `treatment_plan`
- Có thể áp dụng nhiều phác đồ đồng thời (primary + secondary)
- Dễ dàng điều chỉnh liều lượng theo từng bệnh nhân

### 4. **Báo cáo và thống kê**
- Thống kê hiệu quả các phác đồ điều trị
- Theo dõi tỷ lệ tuân thủ điều trị
- Phân tích xu hướng kê đơn thuốc

### 5. **Tích hợp với VNPAY và Email System**
- Thanh toán thuốc theo prescription
- Email nhắc nhở uống thuốc theo protocol schedule
- Thông báo tái khám theo monitoring_schedule

## NEXT STEPS IMPLEMENTATION

1. **Backend API Updates:**
   - Cập nhật MedicalRecord entity với protocol fields
   - Tạo API endpoints cho protocol management
   - Integration với prescription workflow

2. **Frontend Form Updates:**
   - Thêm dropdown chọn protocol trong examination form
   - Hiển thị thuốc từ protocol khi kê đơn
   - Protocol monitoring dashboard

3. **Notification System:**
   - Schedule reminders theo protocol monitoring
   - Email templates cho medication adherence
   - Integration với existing appointment reminder system

4. **Reporting Dashboard:**
   - Protocol effectiveness reports
   - Patient adherence tracking
   - Drug utilization statistics
