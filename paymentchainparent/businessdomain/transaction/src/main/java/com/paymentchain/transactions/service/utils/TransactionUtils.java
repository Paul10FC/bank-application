package com.paymentchain.transactions.service.utils;

import com.paymentchain.transactions.entities.Status;
import com.paymentchain.transactions.entities.Transaction;
import com.paymentchain.transactions.service.exception.BusinessRuleException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
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
    public static void validateInputs(Transaction input) throws BusinessRuleException {
        boolean isEmptyDate = input.getDate() == null;
        boolean isEmptyChannel = input.getChannel() == null;
        boolean isEmptyIban = input.getAccountIban() == null;
        boolean isEmptyAmount = input.getAmount() == 0;

        if (isEmptyAmount || isEmptyChannel || isEmptyIban || isEmptyDate){
            throw new BusinessRuleException("12312345", "Date, Channel, Amount and Iban can't be empty", HttpStatus.PRECONDITION_FAILED);
        }
    }
    public static void validateAccount(Optional<Double> amount) throws BusinessRuleException {

        if (amount.isEmpty()){
            throw new BusinessRuleException("2314", "The account doesn't exist", HttpStatus.PRECONDITION_FAILED);
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

    public static Transaction setNewTransaction(Transaction transactionInput, Double customerAmount) throws BusinessRuleException {

        String referenceNumber = getRandomReferenceNumber();
        Status status = getStatus(transactionInput.getDate());
        int fee = getFee();
        double amount = validateAmount(transactionInput.getAmount(), fee, customerAmount);

        transactionInput.setReference(referenceNumber);
        transactionInput.setStatus(status);
        transactionInput.setAmount(amount);
        transactionInput.setFee(fee);

        return transactionInput;
    }
}
