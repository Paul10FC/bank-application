/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paymentchain.product.web.controller;
import com.paymentchain.product.entities.Product;
import com.paymentchain.product.respository.ProductRepository;
import com.paymentchain.product.service.ProductService;
import com.paymentchain.product.web.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductRestController {

ProductService service;

@Autowired
ProductRestController(ProductService productService){
    this.service = productService;
}

    @GetMapping()
    public ResponseEntity<List<Product>> getAll() {
        Optional<List<Product>> allProducts = this.service.getAllProducts();

        return allProducts
                .map(products -> new ResponseEntity<>(products, HttpStatus.FOUND))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getOneProduct(@PathVariable Long id) {
        Optional<Product> productOptional = this.service.getOneProduct(id);
        return productOptional
                .map(product -> new ResponseEntity<>(product, HttpStatus.FOUND))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product input) {
        Optional<Product> productUpdated = this.service.updateProduct(id, input);

        return productUpdated
                .map(product -> new ResponseEntity<>(product, HttpStatus.CREATED))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @PostMapping
    public ResponseEntity<?> createNewProduct(@RequestBody Product input) throws BusinessRuleException {
      Optional<Product> newProduct = this.service.newProduct(input);

      return newProduct
              .map(product -> new ResponseEntity<>(product, HttpStatus.CREATED))
              .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<Product> productDeleted = this.service.deleteProduct(id);

        return productDeleted
                .map(product -> new ResponseEntity<>(product, HttpStatus.BAD_GATEWAY))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.OK));
    }
}
