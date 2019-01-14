package unidue.ub.services.getter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import unidue.ub.media.contracts.Contract;
import unidue.ub.services.getter.getter.InvoiceGetter;
import unidue.ub.services.getter.getter.JournalInfoGetter;
import unidue.ub.services.getter.getter.OrderGetter;
import unidue.ub.services.getter.model.RawContract;
import unidue.ub.services.getter.model.RawInvoice;
import unidue.ub.services.getter.model.RawOrder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Controller
public class JournalController {

    private final InvoiceGetter invoiceGetter;

    private final OrderGetter orderGetter;

    private final JournalInfoGetter journalInfoGetter;

    private final static Logger log = LoggerFactory.getLogger(JournalController.class);

    public JournalController(InvoiceGetter invoiceGetter, OrderGetter orderGetter, JournalInfoGetter journalInfoGetter) {
        this.invoiceGetter = invoiceGetter;
        this.orderGetter = orderGetter;
        this.journalInfoGetter = journalInfoGetter;
    }


    @GetMapping("journalcontract/{orderNumber}")
    public ResponseEntity<?> getOrdersForOrderNumber(@PathVariable String orderNumber) {
        log.info("retrieving orders and invoices for order number " + orderNumber);
        List<Contract> contracts = new ArrayList<>();
        List<RawOrder> orders = orderGetter.findOrdersByOrderNumber(orderNumber);
        log.info(String.valueOf(orders.size()) +  " orders found");
        List<RawContract> rawContracts = buildRawContracs(orders);
        rawContracts.forEach(entry -> contracts.add(entry.getContract()));
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("journalcontractByIssn/{issn}")
    public ResponseEntity<?> getOrdersForIssn(@PathVariable String issn) {
        log.info("retrieving orders and invoices for ISSN " + issn);
        List<Contract> contracts = new ArrayList<>();
        List<RawOrder> orders = orderGetter.findOrdersByIssn(issn);
        log.info(String.valueOf(orders.size()) +  " orders found");
        List<RawContract> rawContracts = buildRawContracs(orders);
        rawContracts.forEach(entry -> contracts.add(entry.getContract()));
        return ResponseEntity.ok(contracts);
    }

    private List<RawContract> buildRawContracs(List<RawOrder> rawOrders) {
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

    @GetMapping("journalcontract/all")
    public ResponseEntity<?> getAllContracts() {
        log.info("retrieving all orders and invoices");
        List<Contract> contracts = new ArrayList<>();
        List<RawOrder> orders = orderGetter.getAllOrders();
        log.info(String.valueOf(orders.size()) +  " orders found");
        List<RawContract> rawContracts = buildRawContracs(orders);
        rawContracts.forEach(entry -> contracts.add(entry.getContract()));
        return ResponseEntity.ok(contracts);
    }
}
