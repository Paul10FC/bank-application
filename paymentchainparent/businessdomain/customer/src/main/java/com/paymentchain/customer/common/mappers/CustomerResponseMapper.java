package com.paymentchain.customer.common.mappers;

import com.paymentchain.customer.entities.dto.CustomerResponse;
import com.paymentchain.customer.entities.dto.ProductsResponse;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerResponseMapper {
    @Mappings({
            @Mapping(target = "productsResponseList", ignore = true),
            @Mapping(target = "transactionsResponseList", source = "transactions")
    })
    CustomerResponse customerToCustomerResponse(Customer customer);

    List<CustomerResponse> customerListToCustomerResponseList(List<Customer> customer);


    default List<ProductsResponse> customerProductToProductResponse(List<CustomerProduct> customerProducts){
        List<ProductsResponse> productsResponseList = new ArrayList<>();

        for (CustomerProduct product : customerProducts) {
            ProductsResponse productsResponse = new ProductsResponse();
            productsResponse.setProductId(product.getProductId());
            productsResponse.setProductName(product.getProductName());
            productsResponseList.add(productsResponse);
        }
        return productsResponseList;
    }
}
