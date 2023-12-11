package com.paymentchain.transactions.entities.dto;

import com.paymentchain.transactions.entities.Channel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private String accountIban;
    private double amount;
    private String description;
    private String date;
    private Channel channel;
}
