package com.paymentchain.customer.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paymentchain.customer.bussines.transactions.BussinesTransaction;
import com.paymentchain.customer.dto.CustomerRequest;
import com.paymentchain.customer.dto.CustomerResponse;
import com.paymentchain.customer.dto.CustomerSimpleResponse;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.common.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

@Tag(name = "Customer API", description = "The API serve all the functionality to manage the customer and his accounts")
@RestController
@RequestMapping("/customer")
public class CustomerRestController {

    BussinesTransaction bussinesTransaction;

    @Autowired
    public CustomerRestController(BussinesTransaction bussinesTransaction) {
        this.bussinesTransaction = bussinesTransaction;
    }


    @Operation(description = "Obtain and return customer information in the database", summary = "Get customer list")
    @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "List with customer found"),
                @ApiResponse(responseCode = "204", description = "Empty list has found")
    })
    @JsonIgnoreProperties(value = "transactionsResponseList")
    @GetMapping()
    public ResponseEntity<List<CustomerSimpleResponse>> getAllCustomers(){
        Optional<List<CustomerSimpleResponse>> customerList = this.bussinesTransaction.getAllCustomers();
        return customerList.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }


    @Operation(description = "Update a customer information with information send it", summary = "Update a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated"),
            @ApiResponse(responseCode = "400", description = "Is something wrong with the information send it")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable long id, @RequestBody CustomerRequest input) {
        Optional<Customer> update = this.bussinesTransaction.update(id, input);
        if (update.isPresent()){
            return ResponseEntity.ok(update.get());
        }
        return ResponseEntity.badRequest().build();
    }


    @Operation(description = "Register a new customer in the database", summary = "Register a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The customer has been register with success")
    })
    @PostMapping("/new")
    public ResponseEntity<?> newCustomer(@RequestBody CustomerRequest inputRequest) throws BusinessRuleException, UnknownHostException {
        Customer save = this.bussinesTransaction.save(inputRequest);
        Optional<CustomerSimpleResponse> response = this.bussinesTransaction.customerToCustomerSimpleResponse(save);
        return new ResponseEntity<>(response.get(), HttpStatus.CREATED);
    }


    @Operation(description = "Delete a customer across his id", summary = "Delete a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer delete with success"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable long id){
        Optional<Customer> customer = this.bussinesTransaction.findCustomerById(id);

        if (customer.isPresent()){
            Optional<CustomerSimpleResponse> response = this.bussinesTransaction.customerToCustomerSimpleResponse(customer.get());
            this.bussinesTransaction.deleteCustomerById(customer.get().getCustomerId());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }


    @Operation(description = "Obtain all the customer information registered in databases", summary = "Get all a customer information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found with his information"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/full")
    public ResponseEntity<CustomerResponse> getCustomerByCode(@RequestParam String code) throws UnknownHostException, BusinessRuleException {
        Optional<CustomerResponse> customer = this.bussinesTransaction.getAllData(code);
        return customer
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @Operation(description = "Find a customer across his iban", summary = "Find customer by iban")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found success"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/iban/{iban}")
    public ResponseEntity<CustomerSimpleResponse> getCustomerByIban(@PathVariable String iban) {
        Optional<CustomerSimpleResponse> customerOptional = this.bussinesTransaction.getCustomerByIban(iban);

        return customerOptional
                .map(customer -> new ResponseEntity<>(customer, HttpStatus.FOUND))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @Operation(
            description = "Modify a customer account amount across iban. Is added if the sign is positive, and subtracted if is negative",
            summary = "Modify customer amount"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer amount modified success"),
            @ApiResponse(responseCode = "404", description = "Customer not found across his iban")
    })
    @PutMapping("/account/{mount}/{iban}")
        public ResponseEntity<CustomerSimpleResponse> modifyAccountMount(@PathVariable Double mount, @PathVariable String iban) throws BusinessRuleException {
        Optional<CustomerSimpleResponse> customerOptional = this.bussinesTransaction.modifyAccountMount(mount, iban);

        return customerOptional
                .map(customer -> new ResponseEntity<>(customer, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
