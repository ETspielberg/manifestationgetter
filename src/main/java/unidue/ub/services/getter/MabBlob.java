package unidue.ub.services.getter;

class MabBlob {
	
	private byte[] mabBlob;
	
	private int mabLength;
	
	MabBlob(byte[] mabBlob, int mabLength) {
		this.mabBlob = mabBlob;
		this.mabLength = mabLength;
	}

	/**
	 * @return the mabBlob
	 */
	byte[] getMabBlob() {
		return mabBlob;
	}

	/**
	 * @return the mabLength
	 */
	int getMabLength() {
		return mabLength;
	}
}
