package com.paymentchain.customer.common.mappers;

import com.paymentchain.customer.entities.dto.CustomerSimpleResponse;
import com.paymentchain.customer.entities.Customer;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerSimpleResponseMapper {

    @Mappings({
            @Mapping(target = "balance", source = "accountMount")
    })
    CustomerSimpleResponse customerToCustomerSimpleResponse(Customer customer);
    List<CustomerSimpleResponse> customerListToCustomerSimpleResponseList(List<Customer> customerList);

    @InheritInverseConfiguration
    @Mappings({
            @Mapping(target = "products", ignore = true),
            @Mapping(target = "transactions", ignore = true)
    })
    Customer customerSimpleResponseToCustomer(CustomerSimpleResponse simpleResponse);
}
