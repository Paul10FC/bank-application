package com.paymentchain.customer.common;

import com.paymentchain.customer.dto.CustomerRequest;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerRequestMapper {

    default List<CustomerProduct> customerRequestProductsToRequestProducts(List<Long> customerRequestProductsList) {
        List<CustomerProduct> customerProductList = new ArrayList<>();

        for (Long productId : customerRequestProductsList) {
            CustomerProduct product = new CustomerProduct();
            product.setProductId(productId);
            customerProductList.add(product);
        }
        return customerProductList;
    }

    @Mappings({
            @Mapping(target = "products", ignore = true)
    })
    Customer customerRequestToCustomer(CustomerRequest source);

}
