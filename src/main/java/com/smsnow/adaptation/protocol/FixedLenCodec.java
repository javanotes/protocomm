package com.smsnow.adaptation.protocol;

public interface FixedLenCodec {

	/**
	 * Return the byte length for the given protocol type.
	 * @param protoClassType
	 * @return
	 * @throws CodecException 
	 */
	<T> int sizeof(Class<T> protoClassType) throws CodecException;
	
	

}