package com.e_gov.lab1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "passport_payments")
@Getter
@Setter
public class PassportPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "cnp", nullable = false)
    private String cnp;

    @Column(name = "address", nullable = false)
    private String address;

    @Override
    public String toString() {
        return "PassportPayment{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", cnp='" + cnp + '\'' +
                ", address='" + address + '\'' +
                ", passportType=" + passportType +
                ", processingFee=" + processingFee +
                ", totalAmount=" + totalAmount +
                '}';
    }

    @Column(name = "passport_type", nullable = false)
    private int passportType;

    @Column(name = "processing_fee", nullable = false)
    private double processingFee;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Lob
    @Column(name = "xml_data")
    private String xmlData;
    @Getter
    @Setter
    @Column(name = "payment_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date paymentDate;


}
