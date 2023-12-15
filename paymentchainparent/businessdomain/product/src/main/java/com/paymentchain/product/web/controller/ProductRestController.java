/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paymentchain.product.web.controller;
import com.paymentchain.product.dto.ProductRequest;
import com.paymentchain.product.dto.ProductResponse;
import com.paymentchain.product.entities.Product;
import com.paymentchain.product.service.ProductService;
import com.paymentchain.product.web.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@Tag(name = "Product API", description = "The API serve all the functionality to manage the bank products")
@RestController
@RequestMapping("/product")
public class ProductRestController {

ProductService service;

@Autowired
ProductRestController(ProductService productService){
    this.service = productService;
}

    @Operation(description = "Obtain and return product information in the database", summary = "Get product list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List with products found"),
            @ApiResponse(responseCode = "204", description = "Empty list has found")
    })
    @GetMapping()
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        Optional<List<ProductResponse>> allProducts = this.service.getAllProducts();

        return allProducts
                .map(products -> new ResponseEntity<>(products, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @Operation(description = "Get a product across his id", summary = "Get a specific product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Optional<ProductResponse> productOptional = this.service.getOneProduct(id);
        return productOptional
                .map(product -> new ResponseEntity<>(product, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(description = "Update a product information across his id", summary = "Update a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product updated"),
            @ApiResponse(responseCode = "404", description = "Product to update not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductRequest input) {
        Optional<ProductResponse> productUpdated = this.service.updateProduct(id, input);

        return productUpdated
                .map(product -> new ResponseEntity<>(product, HttpStatus.CREATED))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(description = "Register a new product in the database", summary = "Register a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product registered"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createNewProduct(@RequestBody ProductRequest input) throws BusinessRuleException {
      Optional<ProductResponse> newProduct = this.service.newProduct(input);

      if (newProduct.isEmpty()){
          return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      } else {
          return new ResponseEntity<>(newProduct.get(), HttpStatus.CREATED);
      }

    }

    @Operation(description = "Delete a product in the database", summary = "Delete product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product deleted"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<Product> productDeleted = this.service.deleteProduct(id);

        return productDeleted
                .map(product -> new ResponseEntity<>(product, HttpStatus.BAD_REQUEST))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.OK));
    }
}
