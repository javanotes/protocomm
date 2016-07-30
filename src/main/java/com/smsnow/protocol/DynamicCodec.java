package com.smsnow.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;

public interface DynamicCodec extends Codec {

	/**
	 * Write a protocol instance to out stream dynamically.
	 * @param protoClass
	 * @param metaData
	 * @param out
	 * @throws CodecException
	 */
	<T> void encode(T protoClass, ProtocolMeta metaData, DataOutputStream out) throws CodecException;
	/**
	 * Write a protocol instance to out stream dynamically.
	 * @param protoClass
	 * @param metaData
	 * @return
	 * @throws CodecException
	 */
	<T> ByteBuffer encode(T protoClass, ProtocolMeta metaData) throws CodecException;
	/**
	 * Read an instance of protocol class from an in stream dynamically.
	 * @param protoClassType
	 * @param metaData
	 * @param in
	 * @return
	 * @throws CodecException
	 */
	<T> T decode(Class<T> protoClassType, ProtocolMeta metaData, DataInputStream in) throws CodecException;
	/**
	 * Read an instance of protocol class from an in stream dynamically.
	 * @param protoClassType
	 * @param metaData
	 * @param in
	 * @return
	 * @throws CodecException
	 */
	<T> T decode(Class<T> protoClassType, ProtocolMeta metaData, ByteBuffer in) throws CodecException;
}
