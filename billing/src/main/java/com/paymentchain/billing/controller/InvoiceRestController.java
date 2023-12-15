/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paymentchain.billing.controller;

import com.paymentchain.billing.common.InvoiceRequestMapper;
import com.paymentchain.billing.common.InvoiceResponseMapper;
import com.paymentchain.billing.dto.InvoiceRequest;
import com.paymentchain.billing.dto.InvoiceResponse;
import com.paymentchain.billing.entities.Invoice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import com.paymentchain.billing.respository.InvoiceRepository;

@Tag(name = "Billing API", description = "The API serve all functionality for management invoices")
@RestController
@RequestMapping("/billing")
public class InvoiceRestController {

    InvoiceRepository customerRepository;
    InvoiceRequestMapper requestMapper;
    InvoiceResponseMapper responseMapper;

    @Autowired
    public InvoiceRestController(InvoiceRepository customerRepository, InvoiceRequestMapper requestMapper, InvoiceResponseMapper responseMapper) {
        this.customerRepository = customerRepository;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
    }

    @Operation(description = "Return all invoices bundled as response", summary = "Return 204 if not data found")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping()
    public List<InvoiceResponse> list() {
        List<Invoice> findAll = customerRepository.findAll();
        return responseMapper.invoiceListToInvoiceResponseList(findAll);
    }
    
    @GetMapping("/{id}")
    public Invoice get(@PathVariable String id) {
       Optional<Invoice> findById = this.customerRepository.findById(Long.valueOf(id));
       return findById.get();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable String id, @RequestBody InvoiceRequest input) {
        Invoice save = null;
        Optional<Invoice> findById = this.customerRepository.findById(Long.valueOf(id));
        if (findById.isPresent()){
            save = this.customerRepository.save(findById.get());
        }
        return ResponseEntity.ok(save);
    }
    
    @PostMapping
    public ResponseEntity<?> post(@RequestBody InvoiceRequest input) {
        Invoice invoiceInput = requestMapper.invoiceRequestToInvoice(input);
        Invoice save = customerRepository.save(invoiceInput);
        InvoiceResponse finalInvoice = responseMapper.invoiceToInvoiceResponse(save);
        return ResponseEntity.ok(finalInvoice);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return null;
    }
    
}
