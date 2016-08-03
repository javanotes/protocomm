package com.smsnow.adaptation.protocol.itoc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.springframework.util.Assert;

import com.smsnow.adaptation.protocol.BufferedFixedLenCodec;
import com.smsnow.adaptation.protocol.CodecException;
import com.smsnow.adaptation.protocol.LengthBasedCodec;
import com.smsnow.adaptation.protocol.ProtocolMeta;
import com.smsnow.adaptation.protocol.StreamedFixedLenCodec;
/**
 * A wrapper class to make use either of streamed or buffered transport, in a configurable manner.
 * @author esutdal
 *
 */
public class ITOCCodecWrapper implements LengthBasedCodec,StreamedFixedLenCodec,BufferedFixedLenCodec {

	private StreamedITOCCodec str = null;
	private BufferedITOCCodec buff = null;
	private LengthBasedCodec fl;
	/**
	 * 
	 * @param useByteBuf
	 */
	public ITOCCodecWrapper(Charset charset, boolean useByteBuf) {
		if(useByteBuf){
			buff = new BufferedITOCCodec(charset);
			fl = buff;
		}
		else{
			str = new StreamedITOCCodec(charset);
			fl = str;
		}
	}
	
	@Override
	public <T> ByteBuffer encode(T protoClass) throws CodecException {
		Assert.notNull(buff, "Not a buffered codec");
		return buff.encode(protoClass);
	}
	@Override
	public <T> T decode(Class<T> protoClassType, ByteBuffer in) throws CodecException {
		Assert.notNull(buff, "Not a buffered codec");
		return buff.decode(protoClassType, in);
	}
	@Override
	public <T> ByteBuffer encode(T protoClass, ProtocolMeta metaData) throws CodecException {
		Assert.notNull(buff, "Not a buffered codec");
		return buff.encode(protoClass, metaData);
	}
	@Override
	public <T> T decode(Class<T> protoClassType, ProtocolMeta metaData, ByteBuffer in) throws CodecException {
		Assert.notNull(buff, "Not a buffered codec");
		return buff.decode(protoClassType, metaData, in);
	}
	@Override
	public <T> void encode(T protoClass, DataOutputStream out) throws CodecException {
		Assert.notNull(str, "Not a streamed codec");
		str.encode(protoClass, out);
		
	}
	@Override
	public <T> T decode(Class<T> protoClassType, DataInputStream in) throws CodecException {
		Assert.notNull(str, "Not a streamed codec");
		return str.decode(protoClassType, in);
	}
	@Override
	public <T> void encode(T protoClass, ProtocolMeta metaData, DataOutputStream out) throws CodecException {
		Assert.notNull(str, "Not a streamed codec");
		str.encode(protoClass, metaData, out);
	}
	@Override
	public <T> T decode(Class<T> protoClassType, ProtocolMeta metaData, DataInputStream in) throws CodecException {
		Assert.notNull(str, "Not a streamed codec");
		return str.decode(protoClassType, metaData, in);
	}

}
