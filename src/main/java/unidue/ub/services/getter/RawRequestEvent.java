package unidue.ub.services.getter;

public class RawRequestEvent {
	
	private String recKey;
	
	private String openDate;
	
	private String openHour;
	
	private String holdDate;
	
	private String pickUpLocation;
	
	private int itemSequence;
	
	public RawRequestEvent() {
	}
	
	public RawRequestEvent(String recKey, String openDate, String openHour, String holdDate, String pickUpLocation) {
		this.recKey = recKey;
		this.setItemSequence(Integer.parseInt(recKey.substring(9, 15)));
		this.openDate = openDate;
		this.openHour = openHour;
		this.holdDate = holdDate;
		this.pickUpLocation = pickUpLocation;
	}
	
	public RawRequestEvent(String recKey, String openDate, String openHour, String pickUpLocation) {
		this.recKey = recKey;
		this.setItemSequence(Integer.parseInt(recKey.substring(9, 15)));
		this.openDate = openDate;
		this.openHour = openHour;
		this.holdDate = "";
		this.pickUpLocation = pickUpLocation;
	}

	/**
	 * @return the recKey
	 */
	public String getRecKey() {
		return recKey;
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
	 * @param recKey the recKey to set
	 */
	public void setRecKey(String recKey) {
		this.recKey = recKey;
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

	/**
	 * @return the itemSequence
	 */
	public int getItemSequence() {
		return itemSequence;
	}

	/**
	 * @param itemSequence the itemSequence to set
	 */
	public void setItemSequence(int itemSequence) {
		this.itemSequence = itemSequence;
	}
	
	
}
