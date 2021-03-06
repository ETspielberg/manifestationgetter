package org.unidue.ub.libintel.getter.model;

public class PrimoData {

    private String recordId = "";

    private String isbn = "";

    private String type = "";

    private String title = "";

    private String doi = "";

    private String authors = "";

    private String edition = "";

    private String year;

    private String format = "";

    private String link = "";

    private String linkThumbnail = "";

    private String fulltextLink = "";

    private String shelfmarks = "";

    public String getFulltextLink() {
        return fulltextLink;
    }

    public String getShelfmarks() {
        return shelfmarks;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public void setShelfmarks(String shelfmarks) {
        this.shelfmarks = shelfmarks;
    }

    public void setFulltextLink(String fulltextLink) {
        this.fulltextLink = fulltextLink;
    }

    public String getLinkThumbnail() {
        return linkThumbnail;
    }

    public void setLinkThumbnail(String linkThumbnail) {
        this.linkThumbnail = linkThumbnail;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getType() {return type;}

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
