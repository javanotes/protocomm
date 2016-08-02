package com.smsnow.adaptation.protocol.itoc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;

import org.springframework.util.Assert;

import com.smsnow.adaptation.protocol.AbstractFixedLenCodec;
import com.smsnow.adaptation.protocol.BufferedFixedLenCodec;
import com.smsnow.adaptation.protocol.CodecException;
import com.smsnow.adaptation.protocol.CodecException.Type;
import com.smsnow.adaptation.protocol.FormatMeta;
import com.smsnow.adaptation.protocol.ProtocolMeta;
/**
 * Encode/Decode a pojo bean class according to ITOC protocol specs.
 * @refer 800-17.0-SPECS-1 FINAL, August, 2008
 * @author esutdal
 *
 */
public class BufferedITOCCodec extends AbstractFixedLenCodec implements BufferedFixedLenCodec {
	final Charset charset;
	public BufferedITOCCodec(Charset charset) {
		this.charset = charset;
	}
	/**
	 * UTF8 charset
	 */
	public BufferedITOCCodec() {
		this(StandardCharsets.UTF_8);
	}
	private static void writeAsNumeric(FormatMeta f, Object o, ByteBuffer out) throws IOException
	{
		switch(f.getLength())
		{
			case 1:
				out.put((byte) o);
				break;
			case 2:
				out.putShort((short) o);
				break;
			case 4:
				out.putInt((int) o);
				break;
			case 8:
				out.putLong((long) o);
				break;
				default:
					throw new IOException("Unexpected number length "+f.getLength()+" for field "+f.getFieldName());
		}
	}
	
	@Override
	protected void writeBytes(FormatMeta f, Object o, ByteBuffer out) throws IOException
	{
		f.checkBounds(o);
		byte[] bytes;
		switch(f.getAttr())
		{
			case NUMERIC:
				writeAsNumeric(f, o, out);
				break;
			case BINARY:
				writeAsNumeric(f, o, out);
				break;
			case TEXT:
				bytes = o.toString().getBytes(charset);
				out.put(bytes, 0, f.getLength());
				break;
			default:
				bytes = new byte[f.getLength()];
				Arrays.fill(bytes, (byte)0);
				out.put(bytes, 0, f.getLength());
				break;
		
		}
	}
	
	private static Number readAsNumeric(FormatMeta f, ByteBuffer in) throws IOException
	{
		switch(f.getLength())
		{
			case 1:
				return in.get();
			case 2:
				return in.getShort();
			case 4:
				return in.getInt();
			case 8:
				return in.getLong();
				default:
					throw new IOException("Unexpected number length "+f.getLength()+" for field "+f.getFieldName());
		}
	}
	
	@Override
	protected Object readBytes(FormatMeta f, ByteBuffer in) throws IOException
	{
		Object ret = null;
		byte[] bytes;
		switch(f.getAttr())
		{
			case NUMERIC:
				ret = readAsNumeric(f, in);
				break;
			case BINARY:
				ret = readAsNumeric(f, in);
				break;
			case TEXT:
				bytes = readFully(in, f.getLength());
				ret = new String(bytes, charset);
				break;
			default:
				bytes = readFully(in, f.getLength());
				break;
		
		}
		return ret;
	}
	
	private static byte[] readFully(ByteBuffer in, int len) throws IOException
	{
		byte[] b = new byte[len];
		in.get(b);
		
		return b;
	}
	
	private void readBytesAndSet(Object p, FormatMeta f, ByteBuffer in) throws ReflectiveOperationException, IOException
	{
		Object o = readBytes(f, in);
		if(f.isDateFld())
		{
			o = toDate(f, o);
		}
		f.getSetter().invoke(p, o);
	}
	
	private <T> T read(Class<T> protoClassType, ByteBuffer in, ProtocolMeta meta) throws ReflectiveOperationException, CodecException 
	{
		T tObj = null;
		try {
			tObj = protoClassType.newInstance();
		} catch (InstantiationException | IllegalAccessException e2) {
			throw e2;
		}
		try {
			int len = in.getInt();
			Assert.isTrue(meta.getSize() == len, "Expecting stream length: "+meta.getSize()+" found: "+len);
		} catch (Exception e2) {
			throw new CodecException(e2, Type.IO_ERR);
		}
		for(Entry<Integer, FormatMeta> e : meta.getFormats().entrySet())
		{
			int off = e.getKey();
			FormatMeta f = e.getValue();
			
			try {
				readBytesAndSet(tObj, f, in);
			} catch (IOException e1) {
				throw new CodecException(off, e1, Type.IO_ERR);
			}
			
		}
		
		return tObj;
	}
	
	private <T> void write(FormatMeta f, T protoClass, ByteBuffer out) throws ReflectiveOperationException, IOException
	{
		Object o = f.getGetter().invoke(protoClass);
		if(o instanceof Date)
		{
			o = fromDate(f, (Date) o);
		}
		
		try {
			writeBytes(f, o, out);
		} catch (IllegalArgumentException e) {
			throw new ReflectiveOperationException(e);
		}

	}
	
	/* (non-Javadoc)
	 * @see com.smsnow.protocol.ICodec#encode(T)
	 */
	@Override
	public <T> ByteBuffer encode(T protoClass) throws CodecException  
	{
		ProtocolMeta meta = getMeta(protoClass.getClass());
		return encode(protoClass, meta);
	}
		
		
	/* (non-Javadoc)
	 * @see com.smsnow.protocol.ICodec#decode(java.lang.Class, java.nio.ByteBuffer)
	 */
	@Override
	public <T> T decode(Class<T> protoClassType, ByteBuffer in) throws CodecException
	{
		ProtocolMeta meta = getMeta(protoClassType);
		return decode(protoClassType, meta, in);
	}
	
	/**
	 * 
	 * @param protoClass
	 * @param metaData
	 * @param out
	 * @throws CodecException
	 */
	public <T> void encode(T protoClass, ProtocolMeta metaData, ByteBuffer out) throws CodecException {
		try {
			Assert.notNull(metaData);
			validate(metaData, protoClass.getClass());
		} catch (Exception e2) {
			throw new CodecException(e2, Type.META_ERR);
		}
		try {
			out.putInt(metaData.getSize());
		} catch (Exception e2) {
			throw new CodecException(e2, Type.IO_ERR);
		}
		for(Entry<Integer, FormatMeta> e : metaData.getFormats().entrySet())
		{
			int off = e.getKey();
			FormatMeta f = e.getValue();
			
			try {
				write(f, protoClass, out);
			} catch (ReflectiveOperationException e1) {
				throw new CodecException(off, e1, Type.BEAN_ERR);
			} catch (IOException e1) {
				throw new CodecException(off, e1, Type.IO_ERR);
			}
		}
		
	}
	private void validate(ProtocolMeta metaData, Class<?> protoType) {
		metaData.validate();
		for(FormatMeta fm : metaData.getFormats().values())
		{
			fm.introspect(protoType);
		}
	}
	@Override
	public <T> ByteBuffer encode(T protoClass, ProtocolMeta metaData) throws CodecException {
		ByteBuffer buf = ByteBuffer.allocate(metaData.getSize());
		encode(protoClass, metaData, buf);
		buf.flip();
		return buf;
	}
	
	@Override
	public <T> T decode(Class<T> protoClassType, ProtocolMeta metaData, ByteBuffer in) throws CodecException {
		try {
			try {
				Assert.notNull(metaData);
				validate(metaData, protoClassType);
			} catch (Exception e2) {
				throw new CodecException(e2, Type.META_ERR);
			}
			return read(protoClassType, in, metaData);
		} catch (CodecException e) {
			throw e;
		} catch (ReflectiveOperationException e) {
			throw new CodecException(e, Type.BEAN_ERR);
		}
	}
	@Override
	public <T> int sizeof(Class<T> protoClassType) throws CodecException {
		ProtocolMeta proto = null;
		try {
			proto = getMeta(protoClassType);
			return proto.getSize();
		} catch (CodecException e) {
			throw e;
		}
	}
	
}
