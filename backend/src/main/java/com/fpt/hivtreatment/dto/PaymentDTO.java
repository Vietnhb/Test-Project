package com.fpt.hivtreatment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String paymentMethod; // 'Tiền mặt', 'Chuyển khoản', 'Thẻ', 'VNPAY'
    private String status; // 'Chờ thanh toán', 'Đang xử lý', 'Đã thanh toán', 'Thất bại', 'Hoàn tiền'
    private String transactionId;
    private String transactionRef;
    private String transactionDate;
    private String bankCode;
    private String cardType;
    private String responseCode;
    private String secureHash;
    private LocalDateTime createdAt;
    private List<Long> labTestOrderIds;
    private String notes;

    // Additional fields for convenience
    private String testTypeName;
    private String orderStatus;

    // List of lab test order details
    private List<Map<String, Object>> labTestOrderDetails;
}