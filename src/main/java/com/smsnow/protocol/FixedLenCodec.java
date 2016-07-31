package com.smsnow.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;

public interface FixedLenCodec {

	/**
	 * Write a protocol instance to out stream.
	 * @param protoClass
	 * @param out
	 * @throws CodecException
	 */
	<T> void encode(T protoClass, DataOutputStream out) throws CodecException;

	/**
	 * Convert a protocol instance to byte buffer.
	 * @param protoClass
	 * @return
	 * @throws CodecException
	 */
	<T> ByteBuffer encode(T protoClass) throws CodecException;

	/**
	 * Read an instance of protocol class from an in stream.
	 * @param <T>
	 * @param protoClassType
	 * @param in
	 * @throws CodecException
	 */
	<T> T decode(Class<T> protoClassType, DataInputStream in) throws CodecException;

	/**
	 * Read an instance of protocol class from input byte buffer.
	 * @param protoClassType
	 * @param in
	 * @return
	 * @throws CodecException
	 */
	<T> T decode(Class<T> protoClassType, ByteBuffer in) throws CodecException;

}