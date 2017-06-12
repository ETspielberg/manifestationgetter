package unidue.ub.services.getter;

public class MabBlob {
	
	private byte[] mabBlob;
	
	private int mabLength;
	
	public MabBlob(byte[] mabBlob, int mabLength) {
		this.mabBlob = mabBlob;
		this.mabLength = mabLength;
	}

	/**
	 * @return the mabBlob
	 */
	public byte[] getMabBlob() {
		return mabBlob;
	}

	/**
	 * @return the mabLength
	 */
	public int getMabLength() {
		return mabLength;
	}

	/**
	 * @param mabBlob the mabBlob to set
	 */
	public void setMabBlob(byte[] mabBlob) {
		this.mabBlob = mabBlob;
	}

	/**
	 * @param mabLength the mabLength to set
	 */
	public void setMabLength(int mabLength) {
		this.mabLength = mabLength;
	}
	
	

}
