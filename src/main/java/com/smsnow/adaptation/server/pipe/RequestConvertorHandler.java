package com.smsnow.adaptation.server.pipe;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smsnow.adaptation.protocol.CodecException;
import com.smsnow.adaptation.protocol.itoc.ITOCCodecWrapper;
import com.smsnow.adaptation.server.RequestHandler;
import com.smsnow.perf.ITOCLogin;

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
	
	
	private Serializable transform(ByteBuffer request) throws CodecException
	{
		return codec.decode(rh.requestMapping(), request);
		
	}
	protected Object readAsStreamed(ByteBuf in) throws CodecException
	{
		log.debug("Reading request bytes");
		byte[] b = new byte[in.readableBytes()];
    	in.readBytes(b);
    	log.debug("Transforming request to object");
    	Object o = codec.decode(ITOCLogin.class, new DataInputStream(new ByteArrayInputStream(b)));
    	log.debug("Transformed request to object");
    	return o;
	}
	protected Object readAsBuffered(ByteBuf in) throws CodecException
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
		
		if (log.isDebugEnabled()) {
			int totalLen = in.getInt(0);
			log.debug("Begin request conversion for LLLL - "+totalLen);
		}
		try {
			out.add(buffCodec ? readAsBuffered(in) : readAsStreamed(in));
		} catch (CodecException e) {
			log.error("-- Codec error --", e);
			out.add(e.getMessage());
		}
    	

	}

}
