package com.paymentchain.billing.common;

import com.paymentchain.billing.dto.InvoiceRequest;
import com.paymentchain.billing.entities.Invoice;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-12-11T14:57:24-0600",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.9 (Private Build)"
)
@Component
public class InvoiceRequestMapperImpl implements InvoiceRequestMapper {

    @Override
    public Invoice invoiceRequestToInvoice(InvoiceRequest source) {
        if ( source == null ) {
            return null;
        }

        Invoice invoice = new Invoice();

        invoice.setCustomerId( source.getCustomer() );
        invoice.setNumber( source.getNumber() );
        invoice.setDetail( source.getDetail() );
        invoice.setAmount( source.getAmount() );

        return invoice;
    }

    @Override
    public List<Invoice> invoiceRequestListToInvoiceList(List<InvoiceRequest> source) {
        if ( source == null ) {
            return null;
        }

        List<Invoice> list = new ArrayList<Invoice>( source.size() );
        for ( InvoiceRequest invoiceRequest : source ) {
            list.add( invoiceRequestToInvoice( invoiceRequest ) );
        }

        return list;
    }

    @Override
    public InvoiceRequest invoiceToInvoiceRequest(Invoice source) {
        if ( source == null ) {
            return null;
        }

        InvoiceRequest invoiceRequest = new InvoiceRequest();

        invoiceRequest.setCustomer( source.getCustomerId() );
        invoiceRequest.setNumber( source.getNumber() );
        invoiceRequest.setDetail( source.getDetail() );
        invoiceRequest.setAmount( source.getAmount() );

        return invoiceRequest;
    }

    @Override
    public List<InvoiceRequest> invoiceListToInvoiceRequestList(List<Invoice> source) {
        if ( source == null ) {
            return null;
        }

        List<InvoiceRequest> list = new ArrayList<InvoiceRequest>( source.size() );
        for ( Invoice invoice : source ) {
            list.add( invoiceToInvoiceRequest( invoice ) );
        }

        return list;
    }
}
