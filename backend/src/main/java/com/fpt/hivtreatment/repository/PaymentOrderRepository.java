package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.LabTestOrder;
import com.fpt.hivtreatment.model.entity.Payment;
import com.fpt.hivtreatment.model.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    /**
     * Find payment orders by payment
     */
    List<PaymentOrder> findByPayment(Payment payment);

    /**
     * Find payment orders by payment ID
     */
    List<PaymentOrder> findByPaymentId(Long paymentId);

    /**
     * Find payment orders by lab test order
     */
    List<PaymentOrder> findByLabTestOrder(LabTestOrder labTestOrder);

    /**
     * Find payment order by lab test order ID
     */
    Optional<PaymentOrder> findByLabTestOrderId(Long labTestOrderId);

    /**
     * Check if a lab test order is already associated with any payment
     */
    boolean existsByLabTestOrderId(Long labTestOrderId);

    /**
     * Find payment orders by payment ID with lab test orders
     */
    @Query("SELECT po FROM PaymentOrder po JOIN FETCH po.labTestOrder lto JOIN FETCH lto.testType WHERE po.payment.id = :paymentId")
    List<PaymentOrder> findByPaymentIdWithLabTestOrders(@Param("paymentId") Long paymentId);
}