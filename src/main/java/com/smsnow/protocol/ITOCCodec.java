package com.smsnow.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;

import org.springframework.util.Assert;

import com.smsnow.protocol.CodecException.Type;
/**
 * Encode/Decode a pojo bean class according to ITOC protocol specs.
 * @refer 800-17.0-SPECS-1 FINAL, August, 2008
 * @author esutdal
 *
 */
public class ITOCCodec extends AbstractCodec implements DynamicCodec {
	
	private void writeAsNumeric(FormatMeta f, Object o, DataOutputStream out) throws IOException
	{
		switch(f.length)
		{
			case 1:
				out.writeByte((byte)o);
				break;
			case 2:
				out.writeShort((short) o);
				break;
			case 4:
				out.writeInt((int) o);
				break;
			case 8:
				out.writeLong((long) o);
				break;
				default:
					throw new IOException("Unexpected number length "+f.length+" for field "+f.getFieldName());
		}
	}
	@Override
	protected void writeBytes(FormatMeta f, Object o, DataOutputStream out) throws IOException
	{
		byte[] bytes;
		switch(f.attr)
		{
			case NUMERIC:
				writeAsNumeric(f, o, out);
				break;
			case BINARY:
				writeAsNumeric(f, o, out);
				break;
			case TEXT:
				bytes = o.toString().getBytes(StandardCharsets.UTF_8);
				out.write(bytes, 0, f.length);
				break;
			default:
				bytes = new byte[f.length];
				Arrays.fill(bytes, (byte)0);
				out.write(bytes);
				break;
		
		}
	}
	private Number readAsNumeric(FormatMeta f, DataInputStream in) throws IOException
	{
		switch(f.length)
		{
			case 1:
				return in.readByte();
			case 2:
				return in.readShort();
			case 4:
				return in.readInt();
			case 8:
				return in.readLong();
				default:
					throw new IOException("Unexpected number length "+f.length+" for field "+f.getFieldName());
		}
	}
	@Override
	protected Object readBytes(FormatMeta f, DataInputStream in) throws IOException
	{
		Object ret = null;
		byte[] bytes;
		switch(f.attr)
		{
			case NUMERIC:
				ret = readAsNumeric(f, in);
				break;
			case BINARY:
				ret = readAsNumeric(f, in);
				break;
			case TEXT:
				bytes = new byte[f.length];
				in.readFully(bytes);
				ret = new String(bytes, StandardCharsets.UTF_8);
				break;
			default:
				bytes = new byte[f.length];
				in.readFully(bytes);
				break;
		
		}
		return ret;
	}
	
	private void invokeSetter(Object p, FormatMeta f, DataInputStream in) throws ReflectiveOperationException, IOException
	{
		Object o = readBytes(f, in);
		if(f.isDateFld)
		{
			o = toDate(f, o);
		}
		f.setter.invoke(p, o);
	}
	private <T> T read(Class<T> protoClassType, DataInputStream in, ProtocolMeta meta) throws ReflectiveOperationException, CodecException 
	{
		T tObj = null;
		try {
			tObj = protoClassType.newInstance();
		} catch (InstantiationException | IllegalAccessException e2) {
			throw e2;
		}
		
		for(Entry<Integer, FormatMeta> e : meta.getFormats().entrySet())
		{
			int off = e.getKey();
			FormatMeta f = e.getValue();
			
			try {
				invokeSetter(tObj, f, in);
			} catch (IOException e1) {
				throw new CodecException(off, e1, Type.IO_ERR);
			}
			
		}
		
		return tObj;
	}
	
	
	private <T> void write(FormatMeta f, T protoClass, DataOutputStream out) throws ReflectiveOperationException, IOException
	{
		Object o = f.getter.invoke(protoClass);
		if(o instanceof Date)
		{
			o = fromDate(f, (Date) o);
		}
		
		writeBytes(f, o, out);

	}
	/* (non-Javadoc)
	 * @see com.smsnow.protocol.ICodec#encode(T, java.io.DataOutputStream)
	 */
	@Override
	public <T> void encode(T protoClass, DataOutputStream out) throws CodecException
	{
		ProtocolMeta meta = getMeta(protoClass.getClass());
		encode(protoClass, meta, out);
		
	}
	/* (non-Javadoc)
	 * @see com.smsnow.protocol.ICodec#encode(T)
	 */
	@Override
	public <T> ByteBuffer encode(T protoClass) throws CodecException  
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encode(protoClass, new DataOutputStream(out));
		return ByteBuffer.wrap(out.toByteArray());
	}
		
	
	
	/* (non-Javadoc)
	 * @see com.smsnow.protocol.ICodec#decode(java.lang.Class, java.io.DataInputStream)
	 */
	@Override
	public <T> T decode(Class<T> protoClassType, DataInputStream in) throws CodecException 
	{
		ProtocolMeta meta = getMeta(protoClassType);
		return decode(protoClassType, meta, in);
	}
	
	/* (non-Javadoc)
	 * @see com.smsnow.protocol.ICodec#decode(java.lang.Class, java.nio.ByteBuffer)
	 */
	@Override
	public <T> T decode(Class<T> protoClassType, ByteBuffer in) throws CodecException
	{
		byte [] b = new byte[in.remaining()];
		in.get(b);
		return decode(protoClassType, new DataInputStream(new ByteArrayInputStream(b)));
	}
	@Override
	public <T> void encode(T protoClass, ProtocolMeta metaData, DataOutputStream out) throws CodecException {
		try {
			Assert.notNull(metaData);
			validate(metaData, protoClass.getClass());
		} catch (Exception e2) {
			throw new CodecException(e2, Type.META_ERR);
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
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encode(protoClass, metaData, new DataOutputStream(out));
		return ByteBuffer.wrap(out.toByteArray());
	}
	@Override
	public <T> T decode(Class<T> protoClassType, ProtocolMeta metaData, DataInputStream in) throws CodecException {
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
	public <T> T decode(Class<T> protoClassType, ProtocolMeta metaData, ByteBuffer in) throws CodecException {
		byte [] b = new byte[in.remaining()];
		in.get(b);
		return decode(protoClassType, metaData, new DataInputStream(new ByteArrayInputStream(b)));
	}
	
}