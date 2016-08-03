package com.smsnow.adaptation.protocol.itoc;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;

import org.springframework.util.Assert;

import com.smsnow.adaptation.dto.common.ApplicationHeader;
import com.smsnow.adaptation.dto.common.ITOCInboundHeader;
import com.smsnow.adaptation.dto.common.ITOCOutboundHeader;
import com.smsnow.adaptation.dto.common.ITOCTrailer;
import com.smsnow.adaptation.protocol.AbstractFixedLenCodec;
import com.smsnow.adaptation.protocol.CodecException;
import com.smsnow.adaptation.protocol.CodecException.Type;
import com.smsnow.adaptation.protocol.FormatMeta;
import com.smsnow.adaptation.protocol.ProtocolMeta;
import com.smsnow.adaptation.protocol.StreamedFixedLenCodec;
/**
 * Encode/Decode a pojo bean class according to ITOC protocol specs.
 * @refer 800-17.0-SPECS-1 FINAL, August, 2008
 * @author esutdal
 *
 */
public class StreamedITOCCodec extends AbstractFixedLenCodec implements StreamedFixedLenCodec {
	final Charset charset;
	public StreamedITOCCodec(Charset charset) {
		this.charset = charset;
	}
	/**
	 * UTF8 charset
	 */
	public StreamedITOCCodec() {
		this(StandardCharsets.UTF_8);
	}
	private static void writeAsNumeric(FormatMeta f, Object o, DataOutputStream out) throws IOException
	{
		switch(f.getLength())
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
					throw new IOException("Unexpected byte length "+f.getLength()+" for field "+f.getFieldName());
		}
	}
	private static void fillBytes(FormatMeta f, DataOutputStream out) throws IOException
	{
		byte[] bytes = new byte[f.getLength()];
		Arrays.fill(bytes, (byte)0);
		out.write(bytes, 0, f.getLength());
	}
	
	private void writeAsObject(FormatMeta f, Object o, DataOutputStream out) throws CodecException, IOException
	{
		if (o != null) {
			encode(o, out);
		}
		else
		{
			fillBytes(f, out);
		}
	}
	@Override
	protected void writeBytes(FormatMeta f, Object o, DataOutputStream out) throws IOException, CodecException
	{
		if(o == null)
		{
			fillBytes(f, out);
			return;
		}
		f.checkBounds(o);
		byte[] bytes;
			
		switch(f.getAttr())
		{
		case APPHEADER:
			writeAsObject(f, o, out);
			break;
		case BINARY:
			writeAsNumeric(f, o, out);
			break;
		case INHEADER:
			writeAsObject(f, o, out);
			break;
		case NUMERIC:
			writeAsNumeric(f, o, out);
			break;
		case OUTHEADER:
			writeAsObject(f, o, out);
			break;
		case TEXT:
			bytes = o.toString().getBytes(charset);
			out.write(bytes, 0, f.getLength());
			break;
		case TRAILER:
			writeAsObject(f, o, out);
			break;
		default:
			fillBytes(f, out);
			break;
			
		
		}
	}
	
	private static Number readAsNumeric(FormatMeta f, DataInputStream in) throws IOException
	{
		switch(f.getLength())
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
					throw new IOException("Unexpected byte length "+f.getLength()+" for field "+f.getFieldName());
		}
	}
	
	@Override
	protected Object readBytes(FormatMeta f, DataInputStream in) throws IOException, CodecException
	{
		Object ret = null;
		byte[] bytes;
	
		
		switch(f.getAttr())
		{
		case APPHEADER:
			bytes = readFully(in, f.getLength());
			ret = decode(ApplicationHeader.class, new DataInputStream(new ByteArrayInputStream(bytes)));
			break;
		case BINARY:
			ret = readAsNumeric(f, in);
			break;
		case INHEADER:
			bytes = readFully(in, f.getLength());
			ret = decode(ITOCInboundHeader.class, new DataInputStream(new ByteArrayInputStream(bytes)));
			break;
		case NUMERIC:
			ret = readAsNumeric(f, in);
			break;
		case OUTHEADER:
			bytes = readFully(in, f.getLength());
			ret = decode(ITOCOutboundHeader.class, new DataInputStream(new ByteArrayInputStream(bytes)));
			break;
		case TEXT:
			bytes = readFully(in, f.getLength());
			ret = new String(bytes, charset);
			break;
		case TRAILER:
			bytes = readFully(in, f.getLength());
			ret = decode(ITOCTrailer.class, new DataInputStream(new ByteArrayInputStream(bytes)));
			break;
		default:
			bytes = readFully(in, f.getLength());
			break;
			
		
		}
		return ret;
	}
	
	private static byte[] readFully(DataInputStream in, int len) throws IOException
	{
		byte[] b = new byte[len];
		int read = 0, totalRead = 0;
		do {
			read = in.read(b, totalRead, (len - totalRead));
			if(read == -1)
				break;
			totalRead += read;
		} while (totalRead < len);
		
		Assert.isTrue(totalRead == len, "Expecting "+len+" bytes. Got "+totalRead);
		return b;
	}
	
	private void readBytesAndSet(Object p, FormatMeta f, DataInputStream in) throws ReflectiveOperationException, IOException, CodecException
	{
		Object o = readBytes(f, in);
		if(f.isDateFld())
		{
			o = toDate(f, o);
		}
		f.getSetter().invoke(p, o);
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
				readBytesAndSet(tObj, f, in);
			} catch (IOException e1) {
				throw new CodecException(off, e1, Type.IO_ERR);
			}
			
		}
		
		return tObj;
	}
	
	
	private <T> void write(FormatMeta f, T protoClass, DataOutputStream out) throws ReflectiveOperationException, IOException, CodecException
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
	 * @see com.smsnow.protocol.ICodec#encode(T, java.io.DataOutputStream)
	 */
	@Override
	public <T> void encode(T protoClass, DataOutputStream out) throws CodecException
	{
		Assert.notNull(protoClass, "Null instance");
		ProtocolMeta meta = getMeta(protoClass.getClass());
		encode(protoClass, meta, out);
		
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
	
	
}
