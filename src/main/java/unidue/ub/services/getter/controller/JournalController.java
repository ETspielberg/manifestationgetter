package unidue.ub.services.getter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import unidue.ub.media.contracts.Contract;
import unidue.ub.services.getter.getter.*;
import unidue.ub.services.getter.model.RawContract;
import unidue.ub.services.getter.model.RawInvoice;
import unidue.ub.services.getter.model.RawOrder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Controller
public class JournalController {

    private final InvoiceGetterFactory invoiceGetterFactory;

    private final OrderGetterFactory orderGetterFactory;

    private final JournalInfoGetterFactory journalInfoGetterFactory;

    private final static Logger log = LoggerFactory.getLogger(JournalController.class);

    public JournalController(InvoiceGetterFactory invoiceGetterFactory, OrderGetterFactory orderGetterFactory, JournalInfoGetterFactory journalInfoGetterFactory) {
        this.invoiceGetterFactory = invoiceGetterFactory;
        this.orderGetterFactory = orderGetterFactory;
        this.journalInfoGetterFactory = journalInfoGetterFactory;
    }


    @GetMapping("journalcontract/{orderNumber}")
    public ResponseEntity<?> getOrdersForOrderNumber(@PathVariable String orderNumber) {
        log.info("retrieving orders and invoices for order number " + orderNumber);
        OrderGetter orderGetter = orderGetterFactory.getObject();
        List<RawOrder> orders = orderGetter.findOrdersByOrderNumber(orderNumber);
        return ResponseEntity.ok(extendToContracts(orders));
    }

    @GetMapping("journalcontractByIssn/{issn}")
    public ResponseEntity<?> getOrdersForIssn(@PathVariable String issn) {
        log.info("retrieving orders and invoices for ISSN " + issn);
        OrderGetter orderGetter = orderGetterFactory.getObject();
        List<RawOrder> orders = orderGetter.findOrdersByIssn(issn);
        return ResponseEntity.ok(extendToContracts(orders));
    }

    private List<RawContract> buildRawContracs(List<RawOrder> rawOrders) {
        InvoiceGetter invoiceGetter = invoiceGetterFactory.getObject();
        JournalInfoGetter journalInfoGetter = journalInfoGetterFactory.getObject();
        List<RawContract> rawContracts = new ArrayList<>();
        for (RawOrder order : rawOrders) {
            String internalId = order.getInternalId();
            RawContract rawContract = new RawContract(internalId);
            rawContract.addOrder(order);
            List<RawInvoice> invoices = invoiceGetter.findInvoicesByInternalId(internalId);
            rawContract.setInvoices(new HashSet<>(invoices));
            rawContract.setJournalInfo(journalInfoGetter.getJournalInfoForInternalId(internalId));
            rawContracts.add(rawContract);
        }
        return rawContracts;
    }

    private List<Contract> extendToContracts(List<RawOrder> orders) {
        List<Contract> contracts = new ArrayList<>();
        log.info("extending " + orders.size() +  " orders");
        List<RawContract> rawContracts = buildRawContracs(orders);
        rawContracts.forEach(entry -> contracts.add(entry.getContract()));
        return contracts;
    }

    @GetMapping("journalcontract/all")
    public ResponseEntity<?> getAllContracts() {
        OrderGetter orderGetter = orderGetterFactory.getObject();
        log.info("retrieving all orders and invoices");
        List<Contract> contracts = new ArrayList<>();
        List<RawOrder> orders = orderGetter.getAllOrders();
        log.info(orders.size() +  " orders found");
        List<RawContract> rawContracts = buildRawContracs(orders);
        rawContracts.forEach(entry -> contracts.add(entry.getContract()));
        return ResponseEntity.ok(contracts);
    }
}
