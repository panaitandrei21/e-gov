package com.e_gov.lab1.repository;

import com.e_gov.lab1.model.PassportPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassportPaymentRepository extends JpaRepository<PassportPayment, Long> {
    @Query("SELECT p.passportType, COUNT(p) FROM PassportPayment p GROUP BY p.passportType")
    List<Object[]> countPassportTypes();

    @Query("SELECT p.passportType, AVG(p.processingFee) FROM PassportPayment p GROUP BY p.passportType")
    List<Object[]> averageProcessingFeesByType();

    @Query("SELECT YEAR(p.paymentDate), MONTH(p.paymentDate), SUM(p.totalAmount) FROM PassportPayment p GROUP BY YEAR(p.paymentDate), MONTH(p.paymentDate)")
    List<Object[]> sumTotalPaymentsByMonth();
}
