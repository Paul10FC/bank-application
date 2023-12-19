package com.paymentchain.transactions.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.transactions.common.TransactionRequestMapper;
import com.paymentchain.transactions.common.TransactionResponseMapper;
import com.paymentchain.transactions.entities.dto.TransactionResponse;
import com.paymentchain.transactions.entities.Transaction;
import com.paymentchain.transactions.entities.dto.TransactionRequest;
import com.paymentchain.transactions.service.exception.BusinessRuleException;
import com.paymentchain.transactions.repository.TransactionRepository;
import com.paymentchain.transactions.service.utils.TransactionUtils;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.paymentchain.transactions.service.utils.TransactionUtils.setNewTransaction;

@Service
public class TransactionService {

    TransactionResponseMapper responseMapper;
    TransactionRepository transactionRepository;
    TransactionRequestMapper requestMapper;
    private final WebClient.Builder CLIENT;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, WebClient.Builder client, TransactionRequestMapper requestMapper, TransactionResponseMapper responseMapper) {
        this.responseMapper = responseMapper;
        this.transactionRepository = transactionRepository;
        this.requestMapper = requestMapper;
        this.CLIENT = client;
    }

    // WebClient requires HttpClient library to work property
    HttpClient client = HttpClient.create()
            // Connection Timeout: is a period within which a connection between a client and a server must be established
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            // Keep alive the connection sending periodic packages
            .option(ChannelOption.SO_KEEPALIVE, true)
            // Wait time between the first keep alive sending
            .option(EpollChannelOption.TCP_KEEPIDLE, 300)
            // Keep alive sending interval
            .option(EpollChannelOption.TCP_KEEPINTVL, 60)
            // Response TimeOut; The maximum time we wait to receive a response after sending a request
            .responseTimeout(Duration.ofSeconds(1))
            // Read and write Timeout: A read timeout occurs when no data was read within a certain
            // Period of time, while the write timeout when a write operation cannot finish at specific time
            .doOnConnected( connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

    public Optional<Transaction> newTransaction(TransactionRequest request) throws UnknownHostException, BusinessRuleException {
        Transaction buildTransaction = this.requestMapper.mapTransactionRequestToTransaction(request);

        TransactionUtils.validateInputs(buildTransaction);
        Optional<Double> customerAmount = getCustomerAmount(buildTransaction.getAccountIban());
        TransactionUtils.validateAccount(customerAmount);

        Transaction newTransaction = setNewTransaction(buildTransaction, customerAmount.get());

        setNewAccountAmount(newTransaction.getAmount(), newTransaction.getAccountIban());
        this.transactionRepository.save(newTransaction);
        return Optional.of(newTransaction);
    }

    public Optional<Transaction> updateTransaction(TransactionRequest input, String reference) throws UnknownHostException, BusinessRuleException {
        Optional<Transaction> actualTransaction = this.transactionRepository.findTransactionByReference(reference);

        if (actualTransaction.isPresent()){

            if (input.getAmount() != 0 ) actualTransaction.get().setAmount(input.getAmount());

            if (input.getDate() != null){
                actualTransaction.get().setDate(input.getDate());
            }

            if (input.getChannel() != null) actualTransaction.get().setChannel(input.getChannel());

            if (input.getDescription() != null) actualTransaction.get().setDescription(input.getDescription());

            if (input.getAccountIban() != null) {

                Optional<String> validateAccount = getAccountIban(input.getAccountIban());
                if (validateAccount.isEmpty()){
                    throw new BusinessRuleException("20023", "The new account doesn't match with any customer", HttpStatus.PRECONDITION_FAILED);
                } else {
                    actualTransaction.get().setAccountIban(validateAccount.get());
                }
            }

            this.transactionRepository.save(actualTransaction.get());
        }
        return actualTransaction;
    }

    public List<TransactionResponse> getAllTransactions(){
        List<Transaction> transactions = this.transactionRepository.findAll();

        return this.responseMapper.mapTransactionListToTransactionResponseList(transactions);
    }

    public List<TransactionResponse> getAllTransactionsByAccount(String ibanAccount) throws UnknownHostException, BusinessRuleException {
        Optional<String> optionalAccount = getAccountIban(ibanAccount);

        if (optionalAccount.isPresent()){
            List<Transaction> transactionList = this.transactionRepository.findTransactionByAccountIban(ibanAccount);
            return this.responseMapper.mapTransactionListToTransactionResponseList(transactionList);
        } else {
            throw new BusinessRuleException("20231", "The iban account doesn't exist", HttpStatus.PRECONDITION_FAILED);
        }
    }

    public Optional<String> getAccountIban(String account) throws UnknownHostException {
        Optional<String> iban;

        try {
            WebClient webClient =
                    this.CLIENT
                            .clientConnector(new ReactorClientHttpConnector(client))
                            .baseUrl("http://businessdomain-customer/customer")
                            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .defaultUriVariables(Collections.singletonMap("url", "http://businessdomain-customer/customer"))
                            .build();

            JsonNode block = webClient.method(HttpMethod.GET).uri("/iban/" + account)
                    .retrieve().bodyToMono(JsonNode.class)
                    .block();

            iban = Optional.of(block.get("iban").asText());
        } catch (WebClientResponseException e) {
            HttpStatus status = e.getStatusCode();
            if (status == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            } else {
                throw new UnknownHostException(e.getMessage());
            }
        }
        return iban;
    }

    public void setNewAccountAmount(Double mount, String iban) throws UnknownHostException {
        String auth = "admin" + ":" + "12345";
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);

        try {
            WebClient build =
                    this.CLIENT
                            .clientConnector(new ReactorClientHttpConnector(client))
                            .baseUrl("http://businessdomain-customer/customer")
                            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .defaultHeader(HttpHeaders.AUTHORIZATION, authHeader)
                            .build();

            Optional<?> account =
                    build.method(HttpMethod.PUT).uri("/account/" + mount + "/" + iban)
                            .retrieve().bodyToFlux(Object.class).next().blockOptional();


        } catch (WebClientResponseException e){
            HttpStatus statusCode = e.getStatusCode();
            if (statusCode != HttpStatus.NOT_FOUND){
                throw new UnknownHostException(e.getMessage());
            }
        }
    }

    public Optional<Double> getCustomerAmount(String iban) throws UnknownHostException {
        String auth = "admin" + ":" + "12345";
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);

        try {
            WebClient build =
                    this.CLIENT
                            .clientConnector(new ReactorClientHttpConnector(client))
                            .baseUrl("http://businessdomain-customer/customer")
                            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .defaultHeader(HttpHeaders.AUTHORIZATION, authHeader)
                            .build();

            JsonNode account =
                    build.method(HttpMethod.GET).uri("/iban/" + iban)
                            .retrieve().bodyToMono(JsonNode.class).block();


            return Optional.of(account.get("balance").asDouble());

        } catch (WebClientResponseException e){
            HttpStatus statusCode = e.getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND){
                return Optional.empty();
            } else {
                throw new UnknownHostException(e.getMessage());
            }
        }
    }
}
