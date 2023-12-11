package com.paymentchain.transactions.service.utils;

import com.paymentchain.transactions.entities.Status;
import com.paymentchain.transactions.entities.Transaction;
import com.paymentchain.transactions.entities.dto.TransactionDTO;
import com.paymentchain.transactions.service.exception.BusinessRuleException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TransactionUtils {

    public static String getRandomReferenceNumber(){
        StringBuilder reference = new StringBuilder();

        for (int i = 0; i < 11; i++) {
            int randomNumber =  (int) Math.floor(Math.random() * 9);
            reference.append(randomNumber);
        }

        return reference.toString();
    }
    public static LocalDateTime parseLocalDateTime(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter).atStartOfDay();
    }
    public static Status getStatus(LocalDateTime date){
        if (date.isAfter(LocalDateTime.now())) {
            return Status.PENDING;
        } else {
            return Status.LIQUIDATED;
        }
    }
    public static int getFee(){
        return (int) Math.floor((Math.random() * 20) + 1);
    }
    public static Double getFinalAmount(double amount, Double fee) {
        return amount - fee;
    }
    public static void validateInputs(TransactionDTO input) throws BusinessRuleException {
        boolean isEmptyDate = input.getDate().isEmpty();
        boolean isEmptyChannel = input.getChannel() == null;
        boolean isEmptyIban = input.getAccountIban() == null;
        boolean isEmptyAmount = input.getAmount() == 0;

        if (isEmptyAmount || isEmptyChannel || isEmptyIban || isEmptyDate){
            throw new BusinessRuleException("12312345", "Date, Channel, Amount and Iban can't be empty", HttpStatus.PRECONDITION_FAILED);
        }
    }
    public static void validateAccount(Optional<?> customer) throws BusinessRuleException {
        if (customer.isEmpty()){
            throw new BusinessRuleException("2314", "The accoutn don't exist", HttpStatus.PRECONDITION_FAILED);
        }
    }
    public static double validateAmount(double transactionAmount, double fee, double customerAmount) throws BusinessRuleException {
        if (transactionAmount > 0) {
            return getFinalAmount(transactionAmount, fee);

        } else if ((customerAmount - Math.abs(transactionAmount)) > 0) {
            return transactionAmount;

        } else {
            throw new BusinessRuleException("123123", "The mount you try is less than the you have", HttpStatus.PRECONDITION_FAILED);

        }
    }

    public static Transaction setNewTransaction(TransactionDTO transactionInput, Double customerAmount) throws BusinessRuleException {
        Transaction newTransaction = new Transaction();

        String referenceNumber = getRandomReferenceNumber();
        LocalDateTime date = parseLocalDateTime(transactionInput.getDate());
        Status status = getStatus(date);
        int fee = getFee();
        double amount = validateAmount(transactionInput.getAmount(), fee, customerAmount);

        newTransaction.setReference(referenceNumber);
        newTransaction.setDate(date);
        newTransaction.setStatus(status);
        newTransaction.setAmount(amount);
        newTransaction.setFee(fee);

        newTransaction.setDescription(transactionInput.getDescription());
        newTransaction.setChannel(transactionInput.getChannel());
        newTransaction.setAccountIban(transactionInput.getAccountIban());

        return newTransaction;
    }
}
