# LUỒNG Y TẾ THỰC TẾ: TEST TRƯỚC - PROTOCOL VÀ THUỐC SAU

## ❌ VẤN ĐỀ HIỆN TẠI - LUỒNG KHÔNG ĐÚNG Y TẾ

**Luồng sai hiện tại:**
```
Medical Record → Chọn Protocol ngay → Kê đơn thuốc
```

**Vấn đề:**
- Bác sĩ không thể chọn protocol điều trị trước khi có kết quả xét nghiệm
- Trong y tế HIV, phải có CD4, Viral Load mới quyết định phác đồ
- Điều trị HIV dựa trên kết quả xét nghiệm chắc chắn

## ✅ LUỒNG Y TẾ ĐÚNG THỰC TẾ

### **GIAI ĐOẠN 1: KHÁM VÀ CHỈ ĐỊNH XÉT NGHIỆM**
```
Bệnh nhân đến khám
    ↓
Bác sĩ tạo Medical Record (lần 1)
    - Khám lâm sàng
    - Triệu chứng, yếu tố nguy cơ
    - Đánh giá sơ bộ
    - primary_protocol_id = NULL (chưa có)
    - secondary_protocol_id = NULL (chưa có)
    ↓
Bác sĩ chỉ định xét nghiệm từ Medical Record
    - HIV sàng lọc
    - Nếu (+) → HIV khẳng định
    - Nếu HIV (+) → CD4, Viral Load, chức năng gan/thận
    ↓
Bệnh nhân thanh toán (VNPAY)
    ↓
Thực hiện xét nghiệm
    ↓
Có kết quả xét nghiệm
```

### **GIAI ĐOẠN 2: TÁI KHÁM VÀ ĐIỀU TRỊ DỰA TRÊN KẾT QUẢ**
```
Bệnh nhân tái khám với kết quả
    ↓
Bác sĩ cập nhật Medical Record (lần 2)
    - Xem xét kết quả xét nghiệm
    - Dựa trên CD4, Viral Load → Chọn Protocol phù hợp
    - primary_protocol_id = X (VD: Phác đồ ARV lần đầu)
    - secondary_protocol_id = Y (VD: Dự phòng nhiễm trùng cơ hội nếu CD4 < 200)
    - protocol_start_date = ngày bắt đầu điều trị
    ↓
Kê đơn thuốc theo Protocol đã chọn
    - Liên kết prescription với medical_record_id và protocol_id
    - Thuốc theo đúng phác đồ chuẩn
    ↓
Cấp phát thuốc và hướng dẫn
    ↓
Lên lịch theo dõi theo monitoring_schedule của protocol
```

## 🛠 ĐIỀU CHỈNH CẦN THIẾT

### 1. **Medical Records Workflow Update**

**Lần khám 1 (Sàng lọc):**
```sql
medical_records {
    visit_type = 'Khám bệnh'
    diagnosis = 'Chờ kết quả xét nghiệm HIV'
    primary_protocol_id = NULL      -- Chưa thể chọn protocol
    secondary_protocol_id = NULL    -- Chưa thể chọn protocol
    protocol_start_date = NULL      -- Chưa bắt đầu điều trị
    treatment_plan = 'Chờ kết quả XN để quyết định phác đồ điều trị'
}
```

**Lần khám 2 (Tái khám với kết quả):**
```sql
medical_records {
    visit_type = 'Tái khám'
    diagnosis = 'HIV dương tính, CD4: 180 cells/µL'
    primary_protocol_id = 1         -- Chọn phác đồ ARV lần đầu
    secondary_protocol_id = 3       -- Chọn dự phòng nhiễm trùng cơ hội
    protocol_start_date = '2024-01-15'  -- Bắt đầu điều trị
    treatment_plan = 'Điều trị ARV + dự phòng nhiễm trùng cơ hội'
}
```

### 2. **Business Logic Rules**

**Rule 1: Protocol chỉ được chọn khi có kết quả xét nghiệm**
```
IF medical_record.diagnosis CONTAINS 'HIV dương tính' 
   AND lab_results.cd4_count IS NOT NULL
   AND lab_results.viral_load IS NOT NULL
THEN allow protocol selection
ELSE primary_protocol_id = NULL
```

**Rule 2: Protocol selection dựa trên kết quả XN**
```
IF cd4_count < 200 
THEN secondary_protocol_id = 3 (dự phòng nhiễm trùng cơ hội)

IF viral_load > 1000 AND treatment_naive = TRUE
THEN primary_protocol_id = 1 (ARV lần đầu)

IF treatment_failure = TRUE AND drug_resistance = TRUE  
THEN primary_protocol_id = 2 (ARV kháng thuốc)
```

### 3. **Frontend Logic Update**

**Examination Form - Lần khám đầu:**
- Protocol selection dropdown = DISABLED
- Message: "Chọn phác đồ điều trị sau khi có kết quả xét nghiệm"

**Examination Form - Tái khám với kết quả:**  
- Hiển thị lab results summary
- Protocol selection dropdown = ENABLED với recommendations
- Auto-suggest protocol dựa trên lab results

### 4. **Database Query Pattern**

**Kiểm tra điều kiện chọn protocol:**
```sql
SELECT 
    mr.id,
    mr.diagnosis,
    COUNT(ltr.id) as lab_results_count,
    MAX(CASE WHEN tt.name LIKE '%HIV%' THEN ltr.result_data END) as hiv_result,
    MAX(CASE WHEN tt.name LIKE '%CD4%' THEN ltr.result_data END) as cd4_result
FROM medical_records mr
LEFT JOIN lab_test_orders lto ON mr.id = lto.medical_record_id  
LEFT JOIN lab_test_results ltr ON lto.id = ltr.lab_test_order_id
LEFT JOIN test_types tt ON lto.test_type_id = tt.id
WHERE mr.patient_id = ?
GROUP BY mr.id
HAVING lab_results_count > 0 
   AND hiv_result IS NOT NULL;
```

## 📋 CẬP NHẬT SAMPLE DATA

### **Medical Record 1 (Sàng lọc) - ĐÚNG:**
```sql
(3, 2, '2024-01-10', 'Khám bệnh', 
 'Đến khám sàng lọc HIV theo định kỳ',
 -- ...clinical examination details...
 'Chờ kết quả xét nghiệm HIV, HBV, HCV để đưa ra kế hoạch điều trị',
 NULL, NULL, NULL,  -- Chưa có protocol nào
 -- ...other fields...
)
```

### **Medical Record 2 (Tái khám với kết quả) - CẬP NHẬT:**
```sql  
(3, 2, '2024-01-14', 'Tái khám',
 'Tái khám để xem kết quả xét nghiệm HIV',
 -- ...clinical examination details... 
 'Bắt đầu điều trị ARV và dự phòng nhiễm trùng cơ hội dựa trên kết quả XN',
 1, 3, '2024-01-15',  -- Có protocol sau khi có kết quả
 -- ...other fields...
)
```

## 🎯 KẾT LUẬN

**Luồng đúng thực tế:**
1. **Khám lần 1** → Medical Record (chưa có protocol) → Chỉ định XN → Thanh toán
2. **Làm XN** → Có kết quả
3. **Tái khám lần 2** → Cập nhật Medical Record (có protocol dựa trên kết quả) → Kê đơn thuốc

**Điều này đảm bảo:**
- ✅ Tuân thủ quy trình y tế đúng chuẩn
- ✅ Protocol selection có cơ sở khoa học (dựa trên lab results)  
- ✅ Không kê đơn thuốc nếu chưa có chẩn đoán chắc chắn
- ✅ Phù hợp với thực tế khám bệnh HIV
