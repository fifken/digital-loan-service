package com.example.digital_loan_service.controller;

import com.example.digital_loan_service.model.LoanEntity;
import com.example.digital_loan_service.model.UserEntity;
import com.example.digital_loan_service.service.LoanService;
import com.example.digital_loan_service.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/loan")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private UserService userService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> applyLoan(@RequestBody LoanEntity loan) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserEntity> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        if (loan.getAmount() == null || loan.getAmount() <= 0) {
            return ResponseEntity.badRequest().body("Invalid amount");
        }

        loan.setUserId(userOpt.get().getId());
        loan.setStatus("PENDING");
        loan.setSubmittedDate(LocalDateTime.now());

        LoanEntity createdLoan = loanService.applyForLoan(loan);
        return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyLoans() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserEntity> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        List<LoanEntity> loans = loanService.getMyLoans(userOpt.get().getId());
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LoanEntity>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveLoan(@PathVariable Long id) {
        boolean updated = loanService.updateLoanStatus(id, "APPROVED");
        if (updated) {
            return ResponseEntity.ok("Loan approved");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Loan not found");
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectLoan(@PathVariable Long id) {
        boolean updated = loanService.updateLoanStatus(id, "REJECTED");
        if (updated) {
            return ResponseEntity.ok("Loan rejected");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Loan not found");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LoanEntity> getLoanDetail(@PathVariable Long id) {
        Optional<LoanEntity> loanOpt = loanService.getLoanById(id);
        return loanOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
