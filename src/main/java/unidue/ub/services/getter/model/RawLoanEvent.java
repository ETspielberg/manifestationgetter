package unidue.ub.services.getter.model;

public class RawLoanEvent {
	
	private String itemId;
	
	private String subLibrary;
	
	private String borrowerStatus;
	
	private String loanDate;
	
	private String loanHour;
	
	private String returnDate;
	
	private String returnHour;
	
	private String materials;
	
	public RawLoanEvent() {
	}
	
	public RawLoanEvent(String itemId, String subLibrary, String borrowerStatus, String material, String loanDate, String loanHour) {
		this.itemId = (itemId.length() > 15) ? itemId.substring(0,15) : itemId;
		this.subLibrary = subLibrary;
		this.borrowerStatus = borrowerStatus;
		this.loanDate = loanDate;
		this.loanHour = loanHour;
		this.returnDate = "";
		this.returnHour = "";
	}
	
	public RawLoanEvent(String itemId, String subLibrary, String borrowerStatus, String material, String loanDate, String loanHour, String returnDate, String returnHour) {
		this.itemId = (itemId.length() > 15) ? itemId.substring(0,15) : itemId;
		this.subLibrary = subLibrary;
		this.borrowerStatus = borrowerStatus;
		this.loanDate = loanDate;
		this.loanHour = loanHour;
		this.returnDate = returnDate;
		this.returnHour = returnHour;
	}
	
	/**
	 * @return the recKey
	 */
	public String getItemId() {
		return itemId;
	}

	/**
	 * @return the subLibrary
	 */
	public String getSubLibrary() {
		return subLibrary;
	}

	/**
	 * @return the borrowerStatus
	 */
	public String getBorrowerStatus() {
		return borrowerStatus;
	}

	/**
	 * @return the loanDate
	 */
	public String getLoanDate() {
		return loanDate;
	}

	/**
	 * @return the loanHour
	 */
	public String getLoanHour() {
		return loanHour;
	}

	/**
	 * @return the returnDate
	 */
	public String getReturnDate() {
		return returnDate;
	}

	/**
	 * @return the returnHour
	 */
	public String getReturnHour() {
		return returnHour;
	}

	/**
	 * @param itemId the recKey to set
	 */
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	/**
	 * @param subLibrary the subLibrary to set
	 */
	public void setSubLibrary(String subLibrary) {
		this.subLibrary = subLibrary;
	}

	/**
	 * @param borrowerStatus the borrowerStatus to set
	 */
	public void setBorrowerStatus(String borrowerStatus) {
		this.borrowerStatus = borrowerStatus;
	}

	/**
	 * @param loanDate the loanDate to set
	 */
	public void setLoanDate(String loanDate) {
		this.loanDate = loanDate;
	}

	/**
	 * @param loanHour the loanHour to set
	 */
	public void setLoanHour(String loanHour) {
		this.loanHour = loanHour;
	}

	/**
	 * @param returnDate the returnDate to set
	 */
	public void setReturnDate(String returnDate) {
		this.returnDate = returnDate;
	}

	/**
	 * @param returnHour the returnHour to set
	 */
	public void setReturnHour(String returnHour) {
		this.returnHour = returnHour;
	}

	/**
	 * @return the materials
	 */
	public String getMaterials() {
		return materials;
	}

	/**
	 * @param materials the materials to set
	 */
	public void setMaterials(String materials) {
		this.materials = materials;
	}

	

}
