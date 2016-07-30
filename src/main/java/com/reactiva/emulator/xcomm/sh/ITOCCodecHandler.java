package com.reactiva.emulator.xcomm.sh;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.Assert;

import com.smsnow.protocol.CodecException;
import com.smsnow.protocol.CodecException.Type;
import com.smsnow.protocol.DynamicCodec;
import com.smsnow.protocol.ITOCCodec;
import com.smsnow.protocol.IType;

public class ITOCCodecHandler {

	private Map<Short, String> codec = new HashMap<>();
	private DynamicCodec codecImpl = new ITOCCodec();
	public Map<Short, String> getCodec() {
		return codec;
	}
	@PostConstruct
	private void validate()
	{
		for(Entry<Short, String> e : codec.entrySet())
		{
			try {
				IType typ = (IType) Class.forName(e.getValue()).newInstance();
				Assert.isTrue(e.getKey() == typ.code(), "message.codec="+e.getKey()+" "+e.getValue()+".code="+typ.code()+" do not match.");
			} catch (Exception e1) {
				throw new BeanCreationException("Invalid message codec "+e.getValue(), e1);
			}
		}
	}
	public void setCodec(Map<Short, String> codec) {
		getCodec().putAll(codec);
	}
	/**
	 * Write a protocol message to output stream bytes.
	 * @param protoClass
	 * @param out
	 * @throws CodecException
	 */
	public <T extends IType> void write(T protoClass, DataOutputStream out) throws CodecException {
		try {
			out.writeShort(protoClass.code());
		} catch (IOException e) {
			throw new CodecException(e, Type.IO_ERR);
		}
		codecImpl.encode(protoClass, out);

	}

	@SuppressWarnings("unchecked")
	private Class<? extends IType> getTypeClass(DataInputStream in) throws IOException, CodecException
	{
		short s = in.readShort();
		if(!getCodec().containsKey(s))
		{
			throw new CodecException(new IllegalArgumentException("Did not find matching protocol class for type- "+s), Type.META_ERR);
		}
		try {
			return (Class<? extends IType>) Class.forName(getCodec().get(s));
		} catch (Exception e) {
			throw new CodecException(e, Type.BEAN_ERR);
		}
	}
	/**
	 * Read from input stream bytes and convert to a corresponding protocol message.
	 * @param in
	 * @return
	 * @throws CodecException
	 */
	public <T extends IType> IType read(DataInputStream in) throws CodecException {
		try 
		{
			Class<? extends IType> type = getTypeClass(in);
			return codecImpl.decode(type, in);
			
		} catch (IOException e) {
			throw new CodecException(e, Type.IO_ERR);
		}
	}


}
