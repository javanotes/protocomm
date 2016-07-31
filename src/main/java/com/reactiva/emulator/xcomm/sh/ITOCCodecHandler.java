package com.reactiva.emulator.xcomm.sh;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.smsnow.protocol.CodecException;
import com.smsnow.protocol.DynamicCodec;
import com.smsnow.protocol.FixedLengthType;
import com.smsnow.protocol.ITOCCodec;

public class ITOCCodecHandler {

	private DynamicCodec codecImpl = new ITOCCodec();
	
	/**
	 * Write a protocol message to output stream bytes.
	 * @param protoClass
	 * @param out
	 * @throws CodecException
	 */
	public <T extends FixedLengthType> void write(T protoClass, DataOutputStream out) throws CodecException {
		codecImpl.encode(protoClass, out);

	}

	/**
	 * Read from input stream bytes and convert to a corresponding protocol message.
	 * @param protoClass
	 * @param in
	 * @return
	 * @throws CodecException
	 */
	public <T extends FixedLengthType> FixedLengthType read(Class<T> protoClass, DataInputStream in) throws CodecException {
		return codecImpl.decode(protoClass, in);
	}


}
