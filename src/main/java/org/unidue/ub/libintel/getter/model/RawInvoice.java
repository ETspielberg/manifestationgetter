package org.unidue.ub.libintel.getter.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import unidue.ub.media.contracts.Invoice;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RawInvoice {

    private String interalId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private String debitCredit;

    private Double totalAmount;

    private String note;

    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date processDate;

    private Double processAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    Date dateFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    Date dateTill;

    public RawInvoice(String interalId, String date, String debitCredit, String totalAmount, String note, String dateFrom, String dateTill, String currency, String processDate, String processAmount) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        this.interalId = interalId;
        try {
            this.date = dateFormat.parse(date);
        } catch (ParseException e) {
            this.date = new Date();
        }
        this.debitCredit = debitCredit;
        this.totalAmount = Double.parseDouble(totalAmount)/100;
        this.note = note;
        try {
            this.dateFrom = dateFormat.parse(dateFrom);
        } catch (ParseException pe) {
            this.dateFrom = new Date();
        }
        try {
            this.dateTill = dateFormat.parse(dateTill);
        } catch (ParseException pe) {
            this.dateTill = new Date();
        }
        this.currency = currency;
        try {
            this.processDate = dateFormat.parse(processDate);
        } catch (ParseException pe) {
            this.processDate = new Date();
        }
        this.processAmount = Double.parseDouble(processAmount);
    }

    public Date getProcessDate() {
        return processDate;
    }

    public void setProcessDate(Date processDate) {
        this.processDate = processDate;
    }

    public Double getProcessAmount() {
        return processAmount;
    }

    public void setProcessAmount(Double processAmount) {
        this.processAmount = processAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDebitCredit() {
        return debitCredit;
    }

    public void setDebitCredit(String debitCredit) {
        this.debitCredit = debitCredit;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTill() {
        return dateTill;
    }

    public void setDateTill(Date dateTill) {
        this.dateTill = dateTill;
    }

    public String getInteralId() {
        return interalId;
    }

    public void setInteralId(String interalId) {
        this.interalId = interalId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Invoice getInvoice() {
        if ("C".equals(debitCredit)) {
            totalAmount = -1 * totalAmount;
        }
        Invoice invoice = new Invoice(date, processDate, dateFrom, dateTill, interalId, totalAmount, currency, note);
        return invoice;
    }
}
