package com.smsnow.adaptation.server.pipe;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.smsnow.adaptation.server.RequestHandler;
import com.smsnow.protocol.CodecException;
import com.smsnow.protocol.FixedLenCodec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class RequestConvertorHandler extends ByteToMessageDecoder {
	private static final Logger log = LoggerFactory.getLogger(RequestConvertorHandler.class);
	/**
	 * Pass all helper class instances, if reqd.
	 * This class is NOT in singleton scope.
	 */
	private FixedLenCodec codec;
	private RequestHandler rh;
	public RequestConvertorHandler(FixedLenCodec codecHdlr, RequestHandler rh)
	{
		this.codec = codecHdlr;
		this.rh = rh;
	}
	/**
	 * Override this method to parse the request convert to an object.
	 * @param request
	 * @return
	 * @throws IOException 
	 * @throws CodecException 
	 */
	protected Serializable transform(DataInputStream request) throws IOException, CodecException
	{
		return codec.decode(rh.requestMapping(), request);
		
	}
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		log.debug("Begin request conversion");
		int size = codec.sizeof(rh.requestMapping());
		int len = in.readableBytes()-4;
		Assert.isTrue(len == size, "Expecting a message of size "+size+". Found "+len);
        
		log.debug("Reading request bytes");
		byte[] b = new byte[in.readableBytes()];
    	in.readBytes(b);
    	log.debug("Transforming request to object");
    	Object o = transform(new DataInputStream(new ByteArrayInputStream(b)));
    	log.debug("Transformed request to object");
    	out.add(o);
    	

	}

}
