package org.unidue.ub.libintel.getter.model;

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
}
