package com.paymentchain.transactions.repository;

import com.paymentchain.transactions.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findTransactionByAccountIban(String account);
}
