package com.paymentchain.transactions.common;

import com.paymentchain.transactions.entities.dto.TransactionRequest;
import com.paymentchain.transactions.entities.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface TransactionRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "reference", ignore = true),
            @Mapping(target = "fee", ignore = true),
            @Mapping(target = "status", ignore = true)
    })
    Transaction mapTransactionRequestToTransaction(TransactionRequest request);
}
