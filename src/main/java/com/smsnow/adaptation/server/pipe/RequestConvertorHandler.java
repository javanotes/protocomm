package com.smsnow.adaptation.server.pipe;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smsnow.adaptation.protocol.CodecException;
import com.smsnow.adaptation.protocol.itoc.ITOCCodecWrapper;
import com.smsnow.adaptation.server.RequestHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class RequestConvertorHandler extends ByteToMessageDecoder {
	private static final Logger log = LoggerFactory.getLogger(RequestConvertorHandler.class);
	/**
	 * Pass all helper class instances, if reqd.
	 * This class is NOT in singleton scope.
	 */
	private ITOCCodecWrapper codec;
	private RequestHandler rh;
	private boolean buffCodec;
	/**
	 * 
	 * @param codecHdlr
	 * @param rh
	 * @param buffCodec
	 */
	public RequestConvertorHandler(ITOCCodecWrapper codecHdlr, RequestHandler rh, boolean buffCodec)
	{
		this.codec = codecHdlr;
		this.rh = rh;
		this.buffCodec = buffCodec;
	}
	
	private Serializable transform(DataInputStream request) throws IOException, CodecException
	{
		return codec.decode(rh.requestMapping(), request);
		
	}
	private Serializable transform(ByteBuffer request) throws IOException, CodecException
	{
		return codec.decode(rh.requestMapping(), request);
		
	}
	protected Object readAsStreamed(ByteBuf in) throws CodecException, IOException
	{
		log.debug("Reading request bytes");
		byte[] b = new byte[in.readableBytes()];
    	in.readBytes(b);
    	log.debug("Transforming request to object");
    	Object o = transform(new DataInputStream(new ByteArrayInputStream(b)));
    	log.debug("Transformed request to object");
    	return o;
	}
	protected Object readAsBuffered(ByteBuf in) throws CodecException, IOException
	{
    	
    	log.debug("Reading request bytes");
    	ByteBuffer buf = ByteBuffer.allocate(in.readableBytes());
    	in.readBytes(buf);
    	buf.flip();
    	log.debug("Transforming request to object");
    	Object o = transform(buf);
    	log.debug("Transformed request to object");
    	return o;
	}
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		log.debug("Begin request conversion");
		
		/*int size = codec.sizeof(rh.requestMapping());
		int len = in.readableBytes()-4;
		Assert.isTrue(len == size, "Expecting a message of size "+size+". Found "+len);*/
		
    	out.add(buffCodec ? readAsBuffered(in) : readAsStreamed(in));
    	

	}

}
