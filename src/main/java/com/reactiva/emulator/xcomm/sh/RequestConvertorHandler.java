package com.reactiva.emulator.xcomm.sh;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import com.smsnow.protocol.CodecException;
import com.smsnow.protocol.IType;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class RequestConvertorHandler extends ByteToMessageDecoder {
	/**
	 * Pass all helper class instances, if reqd.
	 * This class is NOT in singleton scope.
	 */
	ITOCCodecHandler codecHdlr;
	public RequestConvertorHandler(ITOCCodecHandler codecHdlr)
	{
		this.codecHdlr = codecHdlr;
	}
	/**
	 * Override this method to parse the request convert to an object.
	 * @param request
	 * @return
	 * @throws IOException 
	 * @throws CodecException 
	 */
	protected IType transform(DataInputStream request) throws IOException, CodecException
	{
		return codecHdlr.read(request);
		
	}
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        byte[] b = new byte[in.readableBytes()];
    	in.readBytes(b);
    	
    	Object o = transform(new DataInputStream(new ByteArrayInputStream(b)));
    	out.add(o);
    	

	}

}
