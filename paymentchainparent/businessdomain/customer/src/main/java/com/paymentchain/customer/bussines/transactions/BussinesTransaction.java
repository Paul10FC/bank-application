package com.paymentchain.customer.bussines.transactions;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.exception.BusinessRuleException;
import com.paymentchain.customer.repository.CustomerRepository;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.ManagedArray;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class BussinesTransaction {

    CustomerRepository customerRepository;
    private final WebClient.Builder CLIENT;

    @Autowired
    public BussinesTransaction(CustomerRepository customerRepository, WebClient.Builder client) {
        this.customerRepository = customerRepository;
        CLIENT = client;
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


    public Optional<List<Customer>> getAllCustomers(){
        List<Customer> customerList = this.customerRepository.findAll();

        if (customerList.isEmpty()){
            return Optional.empty();
        } else {
            return Optional.of(customerList);
        }
    }

    public Optional<Customer> getCustomerByIban(String iban){
        return this.customerRepository.getCustomerByIban(iban);
    }
    public Optional<Customer> findCustomerById(long id){
        return this.customerRepository.findById(id);
    }

    public void deleteCustomerById(long id){
        this.customerRepository.deleteById(id);
    }

    public Optional<Customer> getAllData(String code) throws UnknownHostException, BusinessRuleException {
        Optional<Customer> customerOptional = customerRepository.getCustomerByCode(code);

        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            for (CustomerProduct product : customer.getProducts()) {
                product.setProductName(getProductName(product.getProductId()));
            }

            // find all transactions
            Optional<List<?>> transactions = getTransactions(customerOptional.get().getIban());

            if (transactions.isEmpty()){
                throw new BusinessRuleException("20212", "This customer don't have transactions", HttpStatus.NO_CONTENT);
            } else {
             customer.setTransactions(transactions.get());
            }

            return Optional.of(customer);
        } else {
            return Optional.empty();
        }
    }

    public Customer save(Customer input) throws BusinessRuleException, UnknownHostException {
        if (!input.getProducts().isEmpty()){
            for (CustomerProduct dto : input.getProducts()) {
                String productName = getProductName(dto.getProductId());
                if (productName.isEmpty()) {
                    throw new BusinessRuleException("1025", "Product not exist", HttpStatus.PRECONDITION_FAILED);
                } else {
                    dto.setCustomer(input);
                }
            }
        }
        return customerRepository.save(input);
    }

    public Optional<Customer> update(Long id, Customer input){
        Optional<Customer> find = this.customerRepository.findById(id);

        if (find.isPresent()){
            find.get().setAccountMount(input.getAccountMount());
            find.get().setCode(input.getCode());
            find.get().setName(input.getName());
            find.get().setIban(input.getIban());
            find.get().setCode(input.getPhone());
            find.get().setSurname(input.getSurname());
            Customer save = this.customerRepository.save(find.get());
            return Optional.of(save);

        } else {
            return Optional.empty();
        }
    }

    public Optional<Customer> modifyAccountMount(Double amount, String iban) throws BusinessRuleException {
        Double feeWithdrawals = 0.98;
        Optional<Customer> customerOptional = customerRepository.getCustomerByIban(iban);

        System.out.println(10 - Math.abs(amount));

        if (customerOptional.isPresent()){
            Customer customer = customerOptional.get();

            if (amount > 0) {
                customer.setAccountMount(customer.getAccountMount() + amount);
            }else if (amount < 0 && (customer.getAccountMount() - Math.abs(amount) - feeWithdrawals) > 0){
                double finalAmount = customer.getAccountMount() - Math.abs(amount) - feeWithdrawals;
                System.out.println(finalAmount);
                customer.setAccountMount(finalAmount);
                System.out.println(customer.getAccountMount());
            } else {
                throw new BusinessRuleException("1231", "You don't have enough money", HttpStatus.PRECONDITION_FAILED);
            }
            Customer save = this.customerRepository.save(customer);
            System.out.println(save.getAccountMount());
            return Optional.of(save);
        }
        return Optional.empty();
    }

    private String getProductName(long id)throws UnknownHostException{
        String name = null;

        try {
            WebClient build =
                    this.CLIENT
                            .clientConnector(new ReactorClientHttpConnector(client))
                            .baseUrl("http://businessdomain-product/product")
                            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .defaultUriVariables(Collections.singletonMap("url", "http://businessdomain-product/product"))
                            .build();


            JsonNode block = build.method(HttpMethod.GET).uri("/" + id)
                    .retrieve().bodyToMono(JsonNode.class).block();

            name = block.get("name").asText();
        } catch (WebClientResponseException e){
            HttpStatus status = e.getStatusCode();
            if (status == HttpStatus.NOT_FOUND) {
                return "";
            } else {
                throw new UnknownHostException(e.getMessage());
            }
        }
        return name;
    }

    private Optional<List<?>> getTransactions(String ibanNumber) throws UnknownHostException {
        try {
            WebClient build =
                    this.CLIENT
                            .clientConnector(new ReactorClientHttpConnector(client))
                            .baseUrl("http://businessdomain-transactions/transactions")
                            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .build();

            List<?> transactionList =
                    build.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder
                            .queryParam("accountIban", ibanNumber)
                            .build())
                            .retrieve().bodyToFlux(Object.class).collectList().block();

            return Optional.of(transactionList);

        } catch (WebClientResponseException e){
            HttpStatus statusCode = e.getStatusCode();
            if (statusCode == HttpStatus.NO_CONTENT){
                return Optional.empty();
            } else {
                throw new UnknownHostException(e.getMessage());
            }
        }
    }
}
