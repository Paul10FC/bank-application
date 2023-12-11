package com.paymentchain.transactions.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.transactions.entities.Status;
import com.paymentchain.transactions.entities.Transaction;
import com.paymentchain.transactions.entities.dto.TransactionDTO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.paymentchain.transactions.service.utils.TransactionUtils.setNewTransaction;
import static com.paymentchain.transactions.service.utils.TransactionUtils.validateAmount;

@Service
public class TransactionService {

    TransactionRepository transactionRepository;
    private final WebClient.Builder CLIENT;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, WebClient.Builder client) {
        this.transactionRepository = transactionRepository;
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

    public Optional<Transaction> newTransaction(TransactionDTO transactionInput) throws UnknownHostException, BusinessRuleException {

        Optional<Double> customerAmount = getCustomerAmount(transactionInput.getAccountIban());
        TransactionUtils.validateInputs(transactionInput);
        TransactionUtils.validateAccount(customerAmount);

        Transaction newTransaction = setNewTransaction(transactionInput, customerAmount.get());

        setNewAccountAmount(newTransaction.getAmount(), newTransaction.getAccountIban());
        this.transactionRepository.save(newTransaction);
        return Optional.of(newTransaction);
    }

    public Optional<Transaction> updateTransaction(TransactionDTO input, long id) throws UnknownHostException, BusinessRuleException {
        Optional<Transaction> actualTransaction = this.transactionRepository.findById(id);

        if (actualTransaction.isPresent()){
            if (input.getAmount() != 0 ) actualTransaction.get().setAmount(input.getAmount());
            if (!input.getDate().isEmpty()){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDateTime dateTime = LocalDate.parse(input.getDate(), formatter).atStartOfDay();
                actualTransaction.get().setDate(dateTime);
            }
            if (input.getChannel() != null) actualTransaction.get().setChannel(input.getChannel());
            if (!input.getDescription().isEmpty()) actualTransaction.get().setDescription(input.getDescription());
            if (!input.getAccountIban().isEmpty()) {
                Optional<String> validateAccount = getAccountIban(input.getAccountIban());
                if (validateAccount.isEmpty()){
                    throw new BusinessRuleException("20023", "The new account don't match with any customer", HttpStatus.PRECONDITION_FAILED);
                } else {
                    actualTransaction.get().setAccountIban(validateAccount.get());
                }
            }
            this.transactionRepository.save(actualTransaction.get());
        }
        return actualTransaction;
    }

    public List<Transaction> getAllTransactions(){
        return this.transactionRepository.findAll();
    }

    public List<Transaction> getAllTransactionsByAccount(String ibanAccount) throws UnknownHostException, BusinessRuleException {
        Optional<String> optionalAccount = getAccountIban(ibanAccount);

        if (optionalAccount.isPresent()){
            return this.transactionRepository.findTransactionByAccountIban(ibanAccount);
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
        try {
            WebClient build =
                    this.CLIENT
                            .clientConnector(new ReactorClientHttpConnector(client))
                            .baseUrl("http://businessdomain-customer/customer")
                            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
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
        try {
            WebClient build =
                    this.CLIENT
                            .clientConnector(new ReactorClientHttpConnector(client))
                            .baseUrl("http://businessdomain-customer/customer")
                            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .build();

            JsonNode account =
                    build.method(HttpMethod.GET).uri("/iban/" + iban)
                            .retrieve().bodyToMono(JsonNode.class).block();


            return Optional.of(account.get("accountMount").asDouble());

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
