package com.example.digital_loan_service.controller;

import com.example.digital_loan_service.model.LoanEntity;
import com.example.digital_loan_service.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/loan")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LoanEntity> applyLoan(@RequestBody LoanEntity loan, @AuthenticationPrincipal UserDetails userDetails) {
        LoanEntity createdLoan = loanService.applyForLoan(loan);
        return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<LoanEntity>> getMyLoans(@AuthenticationPrincipal UserDetails userDetails) {;
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<List<LoanEntity>> getAllLoans() {
        List<LoanEntity> loans = loanService.getAllLoans();
        return ResponseEntity.ok(loans);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanEntity> approveLoan(@PathVariable Long id) {
        LoanEntity approvedLoan = loanService.approveLoan(id);
        if (approvedLoan != null) {
            return ResponseEntity.ok(approvedLoan);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanEntity> rejectLoan(@PathVariable Long id) {
        LoanEntity rejectedLoan = loanService.rejectLoan(id);
        if (rejectedLoan != null) {
            return ResponseEntity.ok(rejectedLoan);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LoanEntity> getLoanDetail(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<LoanEntity> loan = loanService.getLoanById(id);
        if (loan.isPresent()) {
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            }
            return ResponseEntity.ok(loan.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}