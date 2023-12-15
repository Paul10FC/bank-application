package com.paymentchain.transactions.common;

import com.paymentchain.transactions.entities.dto.TransactionResponse;
import com.paymentchain.transactions.entities.Transaction;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionResponseMapper {
    TransactionResponse mapTransactionToTransactionResponse(Transaction transaction);
    List<TransactionResponse> mapTransactionListToTransactionResponseList(List<Transaction> transactionList);
}
