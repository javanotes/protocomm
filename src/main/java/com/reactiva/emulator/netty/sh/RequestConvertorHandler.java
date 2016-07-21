package com.reactiva.emulator.netty.sh;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.reactiva.emulator.netty.dto.Request;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class RequestConvertorHandler extends ByteToMessageDecoder {
	/**
	 * Pass all helper class instances, if reqd.
	 * This class is NOT in singleton scope.
	 */
	public RequestConvertorHandler()
	{
		//TODO
	}
	/**
	 * Override this method to parse the request convert to an object.
	 * @param request
	 * @return
	 * @throws IOException 
	 */
	protected Request transform(DataInputStream request) throws IOException
	{
		//TODO: override
		int len = request.readInt();
		byte[] b = new byte[len-4];
		request.readFully(b);
		Request r = new Request();
		r.setPayload(new String(b, StandardCharsets.UTF_8));
		
		return r;
		
	}
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        byte[] b = new byte[in.readableBytes()];
    	in.readBytes(b);
    	
    	Object o = transform(new DataInputStream(new ByteArrayInputStream(b)));
    	out.add(o);
    	

	}

}
