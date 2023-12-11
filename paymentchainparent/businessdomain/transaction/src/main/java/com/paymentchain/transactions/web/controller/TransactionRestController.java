package com.paymentchain.transactions.web.controller;

import com.paymentchain.transactions.entities.dto.TransactionDTO;
import com.paymentchain.transactions.entities.Transaction;
import com.paymentchain.transactions.service.exception.BusinessRuleException;
import com.paymentchain.transactions.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.*;

@RestController
@RequestMapping("/transactions")
public class TransactionRestController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionRestController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/new")
    public ResponseEntity<?> createTransaction(@RequestBody TransactionDTO transaction) throws BusinessRuleException, UnknownHostException {
        Optional<Transaction> transactionResult = this.transactionService.newTransaction(transaction);

        if (transactionResult.isPresent()){
            return ResponseEntity.ok(transactionResult.get());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateTransaction(TransactionDTO input, @RequestParam Long id) throws BusinessRuleException, UnknownHostException {
        Optional<Transaction> transaction = this.transactionService.updateTransaction(input, id);
       if (transaction.isPresent()){
           return ResponseEntity.ok(transaction.get());
       } else {
           return ResponseEntity.notFound().build();
       }
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions(){
        List<Transaction> transactionList = this.transactionService.getAllTransactions();
        if (transactionList.isEmpty()){
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(transactionList);
        }
    }

    @GetMapping("/customer")
    public ResponseEntity<List<Transaction>> findTransactionByAccount(@RequestParam String ibanAccount) throws BusinessRuleException, UnknownHostException {
        List<Transaction> transactionList = this.transactionService.getAllTransactionsByAccount(ibanAccount);
        if (transactionList.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(transactionList, HttpStatus.FOUND);
        }
    }
}
