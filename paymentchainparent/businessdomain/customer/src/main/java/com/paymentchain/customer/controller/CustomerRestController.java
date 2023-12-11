package com.paymentchain.customer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.bussines.transactions.BussinesTransaction;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.exception.BusinessRuleException;
import com.paymentchain.customer.repository.CustomerRepository;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/customer")
public class CustomerRestController {

    BussinesTransaction bussinesTransaction;

    @Value("${user.role}")
    private String role;

    @Autowired
    public CustomerRestController(BussinesTransaction bussinesTransaction) {
        this.bussinesTransaction = bussinesTransaction;
    }

    @GetMapping()
    public ResponseEntity<List<Customer>> getAllCustomers(){
        Optional<List<Customer>> customerList = this.bussinesTransaction.getAllCustomers();
        return customerList.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/hello")
    public String sayHello(){
        return this.role;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable long id, @RequestBody Customer input) throws BusinessRuleException {
        Optional<Customer> update = this.bussinesTransaction.update(id, input);
        if (update.isPresent()){
            return ResponseEntity.ok(update.get());
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/new")
    public ResponseEntity<?> newCustomer(@RequestBody Customer input) throws BusinessRuleException, UnknownHostException {
        Customer save = this.bussinesTransaction.save(input);
        return new ResponseEntity<>(save, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable long id){
        Optional<Customer> customer = this.bussinesTransaction.findCustomerById(id);

        if (customer.isPresent()){
            this.bussinesTransaction.deleteCustomerById(customer.get().getId());
            return ResponseEntity.ok(customer.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/full")
    public ResponseEntity<Customer> getCustomerByCode(@RequestParam String code) throws UnknownHostException, BusinessRuleException {
        Optional<Customer> customer = this.bussinesTransaction.getAllData(code);
        return customer
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/iban/{iban}")
    public ResponseEntity<Customer> getCustomerByIban(@PathVariable String iban) {
        Optional<Customer> customerOptional = this.bussinesTransaction.getCustomerByIban(iban);

        return customerOptional
                .map(customer -> new ResponseEntity<>(customer, HttpStatus.FOUND))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/account/{mount}/{iban}")
    public ResponseEntity<Customer> modifyAccountMount(@PathVariable Double mount, @PathVariable String iban) throws BusinessRuleException {
        Optional<Customer> customerOptional = this.bussinesTransaction.modifyAccountMount(mount, iban);

        return customerOptional
                .map(customer -> new ResponseEntity<>(customer, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


}
