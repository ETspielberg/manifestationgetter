package unidue.ub.services.getter;

public class RawRequestEvent {
	
	private String itemId;
	
	private String openDate;
	
	private String openHour;
	
	private String holdDate;
	
	private String pickUpLocation;
	
	public RawRequestEvent() {
	}
	
	public RawRequestEvent(String itemId, String openDate, String openHour, String holdDate, String pickUpLocation) {
		this.itemId = (itemId.length() > 15) ? itemId.substring(0,15) : itemId;
		this.openDate = openDate;
		this.openHour = openHour;
		this.holdDate = holdDate;
		this.pickUpLocation = pickUpLocation;
	}
	
	public RawRequestEvent(String itemId, String openDate, String openHour, String pickUpLocation) {
		this.itemId = (itemId.length() > 15) ? itemId.substring(0,15) : itemId;
		this.openDate = openDate;
		this.openHour = openHour;
		this.holdDate = "";
		this.pickUpLocation = pickUpLocation;
	}

	/**
	 * @return the itemId
	 */
	public String getItemId() {
		return itemId;
	}

	/**
	 * @return the openDate
	 */
	public String getOpenDate() {
		return openDate;
	}

	/**
	 * @return the openHour
	 */
	public String getOpenHour() {
		return openHour;
	}

	/**
	 * @return the holdDate
	 */
	public String getHoldDate() {
		return holdDate;
	}

	/**
	 * @return the pickUpLocation
	 */
	public String getPickUpLocation() {
		return pickUpLocation;
	}

	/**
	 * @param itemId the recKey to set
	 */
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	/**
	 * @param openDate the openDate to set
	 */
	public void setOpenDate(String openDate) {
		this.openDate = openDate;
	}

	/**
	 * @param openHour the openHour to set
	 */
	public void setOpenHour(String openHour) {
		this.openHour = openHour;
	}

	/**
	 * @param holdDate the holdDate to set
	 */
	public void setHoldDate(String holdDate) {
		this.holdDate = holdDate;
	}

	/**
	 * @param pickUpLocation the pickUpLocation to set
	 */
	public void setPickUpLocation(String pickUpLocation) {
		this.pickUpLocation = pickUpLocation;
	}

}
