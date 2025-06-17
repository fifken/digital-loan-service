package com.example.digital_loan_service.service;

import  com.example.digital_loan_service.model.LoanEntity;
import  com.example.digital_loan_service.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    public LoanEntity applyForLoan(LoanEntity loan) {
        loan.setStatus("PENDING");
        loan.setSubmittedDate(LocalDateTime.now());
        return loanRepository.save(loan);
    }

    public List<LoanEntity> getMyLoans(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    public List<LoanEntity> getAllLoans() {
        return loanRepository.findAll();
    }

    public Optional<LoanEntity> getLoanById(Long id) {
        return loanRepository.findById(id);
    }

    public LoanEntity approveLoan(Long loanId) {
        Optional<LoanEntity> optionalLoan = loanRepository.findById(loanId);
        if (optionalLoan.isPresent()) {
            LoanEntity loan = optionalLoan.get();
            loan.setStatus("APPROVED");
            return loanRepository.save(loan);
        }
        return null; // Or throw an exception
    }

    public LoanEntity rejectLoan(Long loanId) {
        Optional<LoanEntity> optionalLoan = loanRepository.findById(loanId);
        if (optionalLoan.isPresent()) {
            LoanEntity loan = optionalLoan.get();
            loan.setStatus("REJECTED");
            return loanRepository.save(loan);
        }
        return null; // Or throw an exception
    }

    public boolean updateLoanStatus(Long loanId, String status) {
        Optional<LoanEntity> optionalLoan = loanRepository.findById(loanId);
        if (optionalLoan.isPresent()) {
            LoanEntity loan = optionalLoan.get();
            loan.setStatus(status);
            loanRepository.save(loan);
            return true;
        }
        return false;
    }
}