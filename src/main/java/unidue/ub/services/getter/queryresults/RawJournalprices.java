package unidue.ub.services.getter.queryresults;

public class RawJournalprices {

    private String amount;

    private String from;

    private String to;

    private String note;

    private String ordernumber;

    private String vendor;

    private String identifier;

    public RawJournalprices(String amount, String from, String to, String note) {
        this.amount = amount;
        this.from = from;
        this.to = to;
        this.note = note;
    }

    public RawJournalprices(String identifier, String vendor , String ordernumber, String amount, String from, String to, String note) {
        this.identifier = identifier;
        this.vendor = vendor;
        this.ordernumber = ordernumber;
        this.amount = amount;
        this.from = from;
        this.to = to;
        this.note = note;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getOrdernumber() {
        return ordernumber;
    }

    public void setOrdernumber(String ordernumber) {
        this.ordernumber = ordernumber;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
