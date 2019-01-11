package unidue.ub.services.getter.model;

import unidue.ub.media.contracts.Contract;
import unidue.ub.media.contracts.Invoice;
import unidue.ub.media.contracts.Order;

import java.util.HashSet;
import java.util.Set;

public class RawContract {

    private String id;

    private Set<RawInvoice> invoices;

    private Set<RawOrder> orders;

    private RawJournalInfo journalInfo;

    public RawContract() {
        this.id = "";
        this.orders = new HashSet<>();
        this.invoices = new HashSet<>();
    }


    public RawContract(String id) {
        this.id = id;
        this.orders = new HashSet<>();
        this.invoices = new HashSet<>();
    }

    public RawJournalInfo getJournalInfo() {
        return journalInfo;
    }

    public void setJournalInfo(RawJournalInfo journalInfo) {
        this.journalInfo = journalInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<RawInvoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(Set<RawInvoice> invoices) {
        this.invoices = invoices;
    }

    public Set<RawOrder> getOrders() {
        return orders;
    }

    public void setOrders(Set<RawOrder> orders) {
        this.orders = orders;
    }

    public void addOrder(RawOrder order) {this.orders.add(order);}

    public Contract getContract() {
        Set<Invoice> finalInvoices = new HashSet<>();
        Set<Order> finalOrders = new HashSet<>();
        this.invoices.forEach(entry -> finalInvoices.add(entry.getInvoice()));
        this.orders.forEach(entry -> finalOrders.add(entry.getOrder()));
        Contract contract = new Contract(journalInfo.title,finalInvoices, finalOrders);
        return contract;
    }
}
