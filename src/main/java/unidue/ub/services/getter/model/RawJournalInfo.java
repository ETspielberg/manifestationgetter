package unidue.ub.services.getter.model;

public class RawJournalInfo {

    String ISSN;

    String title;

    String publisher;

    String author;

    public RawJournalInfo(String ISSN, String title, String publisher, String author) {
        this.ISSN = ISSN;
        this.title = title;
        this.publisher = publisher;
        this.author = author;
    }

    public String getISSN() {
        return ISSN;
    }

    public void setISSN(String ISSN) {
        this.ISSN = ISSN;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
