package com.paymentchain.billing.common;

import com.paymentchain.billing.dto.InvoiceResponse;
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
public class InvoiceResponseMapperImpl implements InvoiceResponseMapper {

    @Override
    public InvoiceResponse invoiceToInvoiceResponse(Invoice source) {
        if ( source == null ) {
            return null;
        }

        InvoiceResponse invoiceResponse = new InvoiceResponse();

        invoiceResponse.setCustomer( source.getCustomerId() );
        invoiceResponse.setInvoiceId( source.getId() );
        invoiceResponse.setNumber( source.getNumber() );
        invoiceResponse.setDetail( source.getDetail() );
        invoiceResponse.setAmount( source.getAmount() );

        return invoiceResponse;
    }

    @Override
    public List<InvoiceResponse> invoiceListToInvoiceResponseList(List<Invoice> source) {
        if ( source == null ) {
            return null;
        }

        List<InvoiceResponse> list = new ArrayList<InvoiceResponse>( source.size() );
        for ( Invoice invoice : source ) {
            list.add( invoiceToInvoiceResponse( invoice ) );
        }

        return list;
    }

    @Override
    public Invoice invoiceResponseToInvoice(InvoiceResponse source) {
        if ( source == null ) {
            return null;
        }

        Invoice invoice = new Invoice();

        invoice.setCustomerId( source.getCustomer() );
        invoice.setId( source.getInvoiceId() );
        invoice.setNumber( source.getNumber() );
        invoice.setDetail( source.getDetail() );
        invoice.setAmount( source.getAmount() );

        return invoice;
    }

    @Override
    public List<Invoice> invoiceResponseToInvoiceList(List<InvoiceResponse> source) {
        if ( source == null ) {
            return null;
        }

        List<Invoice> list = new ArrayList<Invoice>( source.size() );
        for ( InvoiceResponse invoiceResponse : source ) {
            list.add( invoiceResponseToInvoice( invoiceResponse ) );
        }

        return list;
    }
}
