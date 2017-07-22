package unidue.ub.services.getter.series;

import java.util.List;

public class ManifestationSeries {

    private String titleID;

    private String edition;

    private String shelfmarks;

    private int numItems;

    private List<Series> series;

    public String getTitleID() {
        return titleID;
    }

    public void setTitleID(String titleID) {
        this.titleID = titleID;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getShelfmarks() {
        return shelfmarks;
    }

    public void setShelfmarks(String shelfmarks) {
        this.shelfmarks = shelfmarks;
    }

    public int getNumItems() {
        return numItems;
    }

    public void setNumItems(int numItems) {
        this.numItems = numItems;
    }

    public List<Series> getSeries() {
        return series;
    }

    public void setSeries(List<Series> series) {
        this.series = series;
    }
}
