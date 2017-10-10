package unidue.ub.services.getter.queryresults;

public class MabBlob {
	
	byte[] mabBlob;
	
	int mabLength;
	
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
}
