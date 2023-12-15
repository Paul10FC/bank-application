package com.paymentchain.transactions.web.controller;

import com.paymentchain.transactions.entities.dto.TransactionRequest;
import com.paymentchain.transactions.entities.dto.TransactionResponse;
import com.paymentchain.transactions.entities.Transaction;
import com.paymentchain.transactions.service.exception.BusinessRuleException;
import com.paymentchain.transactions.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.*;

@Tag(name = "Transactions API", description = "The API serve all the functionality to manage transactions")
@RestController
@RequestMapping("/transactions")
public class TransactionRestController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionRestController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(description = "Create a new transaction from a customer iban", summary = "New transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction created"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping("/new")
    public ResponseEntity<?> createTransaction(@RequestBody TransactionRequest transaction) throws BusinessRuleException, UnknownHostException {
        Optional<Transaction> transactionResult = this.transactionService.newTransaction(transaction);

        if (transactionResult.isPresent()){
            return ResponseEntity.ok(transactionResult.get());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(description = "Update a transaction from a transaction id", summary = "Update transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction updated"),
            @ApiResponse(responseCode = "404", description = "Transaction to update not found")
    })
    @PutMapping("/update")
    public ResponseEntity<?> updateTransaction(TransactionRequest input, @RequestParam String reference) throws BusinessRuleException, UnknownHostException {
        Optional<Transaction> transaction = this.transactionService.updateTransaction(input, reference);
       if (transaction.isPresent()){
           return ResponseEntity.ok(transaction.get());
       } else {
           return ResponseEntity.notFound().build();
       }
    }

    @Operation(description = "Get all transactions from database", summary = "Get all transactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction list found"),
            @ApiResponse(responseCode = "204", description = "Empty transaction list")
    })
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(){
        List<TransactionResponse> transactionList = this.transactionService.getAllTransactions();
        if (transactionList.isEmpty()){
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(transactionList);
        }
    }

    @Operation(description = "Get a list of transactions by customer across iban", summary = "Get customer transactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction list found"),
            @ApiResponse(responseCode = "204", description = "Empty transaction list")
    })
    @GetMapping("/customer")
    public ResponseEntity<List<TransactionResponse>> findTransactionByAccount(@RequestParam String ibanAccount) throws BusinessRuleException, UnknownHostException {
        List<TransactionResponse> transactionList = this.transactionService.getAllTransactionsByAccount(ibanAccount);
        if (transactionList.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(transactionList, HttpStatus.OK);
        }
    }
}
