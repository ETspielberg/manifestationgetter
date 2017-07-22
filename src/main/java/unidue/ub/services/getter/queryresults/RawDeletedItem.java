package unidue.ub.services.getter.queryresults;

import unidue.ub.media.monographs.Item;

public class RawDeletedItem {

    private String collection;

    private String shelfmark;

    private String subLibrary;

    private String material;

    private String itemStatus;

    private String processStatus;

    private String inventoryDate;

    private String updateDate;

    private String hDate;

    private String price;

    private String itemId;

    private String reasonType;

    public RawDeletedItem(String itemId, String collection, String shelfmark, String subLibrary, String material,
                String itemStatus, String processStatus, String inventoryDate, String updateDate, String hDate, String price) {
        this.collection = collection;
        this.shelfmark = shelfmark;
        this.subLibrary = subLibrary;
        this.material = material;
        this.itemStatus = itemStatus;
        this.processStatus = processStatus;
        this.inventoryDate = inventoryDate;
        this.updateDate = updateDate;
        this.hDate = hDate;
        this.price = price;
        this.itemId = itemId;
        this.reasonType = "";
    }

    public RawDeletedItem(String itemId, String collection, String shelfmark, String subLibrary, String material,
                          String itemStatus, String processStatus, String inventoryDate, String updateDate, String hDate, String price, String reasonType) {
        this(itemId, collection, shelfmark, subLibrary, material, itemStatus, processStatus, inventoryDate, updateDate, hDate, price);
        this.reasonType = reasonType;
    }

        public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getShelfmark() {
        return shelfmark;
    }

    public void setShelfmark(String shelfmark) {
        this.shelfmark = shelfmark;
    }

    public String getSubLibrary() {
        return subLibrary;
    }

    public void setSubLibrary(String subLibrary) {
        this.subLibrary = subLibrary;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(String processStatus) {
        this.processStatus = processStatus;
    }

    public String getInventoryDate() {
        return inventoryDate;
    }

    public void setInventoryDate(String inventoryDate) {
        this.inventoryDate = inventoryDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String gethDate() {
        return hDate;
    }

    public void sethDate(String hDate) {
        this.hDate = hDate;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getReasonType() {
        return reasonType;
    }

    public void setReasonType(String reasonType) {
        this.reasonType = reasonType;
    }

    public Item getItem() {
        Item item = new Item(itemId,collection, shelfmark, subLibrary, material,
                itemStatus, processStatus, inventoryDate, hDate, price);
        if ((itemStatus.equals("89") || itemStatus.equals("90") || itemStatus.equals("xx"))) {
            item.setDeletionDate(updateDate);
        }
        return item;
    }
}
