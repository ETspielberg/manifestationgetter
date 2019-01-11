package unidue.ub.services.getter.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import unidue.ub.media.contracts.Order;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RawOrder {

    private String internalId;
    private String orderNumber;
    private String methodOfAquisition;
    private String note;
    private String vendorCode;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date subscriptionDate;
    private String subLibrary;
    private String currency;
    private String calculatedPrice;

    public RawOrder(String internalId, String orderNumber, String methodOfAquisition, String note, String vendorCode, String subscriptionDate, String subLibrary, String currency, String calculatedPrice) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        this.internalId = internalId;
        this.orderNumber = orderNumber;
        this.methodOfAquisition = methodOfAquisition;
        this.note = note;
        this.vendorCode = vendorCode;
        try {
            this.subscriptionDate = dateFormat.parse(subscriptionDate);
        } catch (ParseException pe) {
            this.subscriptionDate = null;
        }
        this.subLibrary = subLibrary;
        this.currency = currency;
        this.calculatedPrice = calculatedPrice;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getMethodOfAquisition() {
        return methodOfAquisition;
    }

    public void setMethodOfAquisition(String methodOfAquisition) {
        this.methodOfAquisition = methodOfAquisition;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public Date getSubscriptionDate() {
        return subscriptionDate;
    }

    public void setSubscriptionDate(Date subscriptionDate) {
        this.subscriptionDate = subscriptionDate;
    }

    public String getSubLibrary() {
        return subLibrary;
    }

    public void setSubLibrary(String subLibrary) {
        this.subLibrary = subLibrary;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCalculatedPrice() {
        return calculatedPrice;
    }

    public void setCalculatedPrice(String calculatedPrice) {
        this.calculatedPrice = calculatedPrice;
    }

    public Order getOrder() {
        Order order =  new Order();
        order.setInternalId(orderNumber);
        return order;
    }
}
