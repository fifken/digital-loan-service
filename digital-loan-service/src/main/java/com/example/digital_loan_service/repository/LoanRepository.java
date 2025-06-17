package com.example.digital_loan_service.repository;

import com.example.digital_loan_service.model.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, Long> {
    List<LoanEntity> findByUserId(Long userId);
}