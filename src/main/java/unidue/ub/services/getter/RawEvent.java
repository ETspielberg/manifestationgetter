package unidue.ub.services.getter;

public class RawEvent {
	
	private String recKey;
	
	private String subLibrary;
	
	private int itemSequence;
	
	private String borrowerStatus;
	
	private String loanDate;
	
	private String loanHour;
	
	public RawEvent() {
	}
	
	public RawEvent(String recKey, String subLibrary, String borrowerStatus, String loanDate, String loanHour) {
		this.recKey = recKey;
		this.subLibrary = subLibrary;
		this.borrowerStatus = borrowerStatus;
		this.loanDate = loanDate;
		this.loanHour = loanHour;
		this.itemSequence = Integer.parseInt(recKey.substring(9));
	}
	
	/**
	 * @return the recKey
	 */
	public String getRecKey() {
		return recKey;
	}

	/**
	 * @return the subLibrary
	 */
	public String getSubLibrary() {
		return subLibrary;
	}

	/**
	 * @return the itemSequence
	 */
	public int getItemSequence() {
		return itemSequence;
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
	 * @param recKey the recKey to set
	 */
	public void setRecKey(String recKey) {
		this.recKey = recKey;
	}

	/**
	 * @param subLibrary the subLibrary to set
	 */
	public void setSubLibrary(String subLibrary) {
		this.subLibrary = subLibrary;
	}

	/**
	 * @param itemSequence the itemSequence to set
	 */
	public void setItemSequence(int itemSequence) {
		this.itemSequence = itemSequence;
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

	private String returnDate;
	
	private String returnHour;

}
