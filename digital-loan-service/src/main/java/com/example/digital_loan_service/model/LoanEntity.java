package com.example.digital_loan_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @JsonProperty("user_id")
    private Long userId; 

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("status")
    private String status;

    @JsonProperty("submitted_date")
    private LocalDateTime submittedDate;
}