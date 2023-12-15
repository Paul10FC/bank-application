package com.paymentchain.customer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.common.mappers.CustomerRequestMapper;
import com.paymentchain.customer.common.mappers.CustomerResponseMapper;
import com.paymentchain.customer.common.mappers.CustomerSimpleResponseMapper;
import com.paymentchain.customer.entities.dto.CustomerRequest;
import com.paymentchain.customer.entities.dto.CustomerResponse;
import com.paymentchain.customer.entities.dto.CustomerSimpleResponse;
import com.paymentchain.customer.entities.dto.ProductsResponse;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.common.exception.BusinessRuleException;
import com.paymentchain.customer.repository.CustomerRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class BussinesTransaction {

    CustomerRepository customerRepository;
    CustomerRequestMapper customerRequestMapper;
    CustomerResponseMapper customerResponseMapper;
    CustomerSimpleResponseMapper customerSimpleResponseMapper;
    private final WebClient.Builder CLIENT;

    @Autowired
    public BussinesTransaction(
            CustomerRepository customerRepository,
            WebClient.Builder client,
            CustomerRequestMapper customerRequestMapper,
            CustomerResponseMapper customerResponseMapper,
            CustomerSimpleResponseMapper customerSimpleResponseMapper
    )
    {
        this.customerRequestMapper = customerRequestMapper;
        this.customerRepository = customerRepository;
        this.customerResponseMapper = customerResponseMapper;
        this.customerSimpleResponseMapper = customerSimpleResponseMapper;
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


    public Optional<List<CustomerSimpleResponse>> getAllCustomers(){
        List<Customer> customersFound = this.customerRepository.findAll();
        List<CustomerSimpleResponse> customerSimpleResponses = this.customerSimpleResponseMapper.customerListToCustomerSimpleResponseList(customersFound);

        if (customerSimpleResponses.isEmpty()){
            return Optional.empty();
        } else {
            return Optional.of(customerSimpleResponses);
        }
    }

    public Optional<CustomerSimpleResponse> getCustomerByIban(String iban){
        Optional<Customer> customer = this.customerRepository.getCustomerByIban(iban);
        return customer.map(value -> this.customerSimpleResponseMapper.customerToCustomerSimpleResponse(value));
    }
    public Optional<Customer> findCustomerById(long id){
        return this.customerRepository.findById(id);
    }

    public Optional<CustomerSimpleResponse> customerToCustomerSimpleResponse(Customer customer){
        return Optional.of(this.customerSimpleResponseMapper.customerToCustomerSimpleResponse(customer));
    }
    public void deleteCustomerById(long id){
        this.customerRepository.deleteById(id);
    }

    public Optional<CustomerResponse> getAllData(String code) throws UnknownHostException, BusinessRuleException {
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

            CustomerResponse customerResponse = this.customerResponseMapper.customerToCustomerResponse(customer);
            List<ProductsResponse> productsResponses = this.customerResponseMapper.customerProductToProductResponse(customer.getProducts());

            customerResponse.setProductsResponseList(productsResponses);
            return Optional.of(customerResponse);
        } else {
            return Optional.empty();
        }
    }

    public Customer save(CustomerRequest inputRequest) throws BusinessRuleException, UnknownHostException {

        Customer input = customerRequestMapper.customerRequestToCustomer(inputRequest);

        List<CustomerProduct> customerProductList = customerRequestMapper.customerRequestProductsToRequestProducts(inputRequest.getProducts());
        input.setProducts(customerProductList);

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

    public Optional<Customer> update(Long id, CustomerRequest input){
        Optional<Customer> find = this.customerRepository.findById(id);

        if (find.isPresent()){
            if (!input.getCode().isEmpty()) find.get().setCode(input.getCode());
            if (!input.getName().isEmpty()) find.get().setName(input.getName());
            if (!input.getIban().isEmpty()) find.get().setIban(input.getIban());
            if (!input.getPhone().isEmpty()) find.get().setCode(input.getPhone());
            if (!input.getSurname().isEmpty()) find.get().setSurname(input.getSurname());
            Customer save = this.customerRepository.save(find.get());
            return Optional.of(save);
        } else {
            return Optional.empty();
        }
    }

    public Optional<CustomerSimpleResponse> modifyAccountMount(Double amount, String iban) throws BusinessRuleException {
        Double feeWithdrawals = 0.98;
        Optional<Customer> customerOptional = customerRepository.getCustomerByIban(iban);

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
            CustomerSimpleResponse response = this.customerSimpleResponseMapper.customerToCustomerSimpleResponse(save);
            return Optional.of(response);
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
