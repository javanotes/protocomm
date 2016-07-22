package com.reactiva.emulator.xcomm.sh;

import java.util.List;

import com.reactiva.emulator.xcomm.dto.Request;
import com.reactiva.emulator.xcomm.dto.Response;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
@Sharable
public class RequestProcessorHandler extends MessageToMessageDecoder<Request> {

	/**
	 * Pass all helper class instances, if reqd. Singleton scoped.
	 */
	public RequestProcessorHandler()
	{
		
	}
	/**
	 * Process the request using some service class.
	 * @param request
	 * @return
	 */
	protected Response doProcess(Request request) throws Exception
	{
		//TODO override
		Response r = new Response();
		r.setPayload("Got request => "+request.getPayload());
		
		return r;
		
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, Request msg, List<Object> out) throws Exception {
		Response r = doProcess(msg);
		out.add(r);
		
	}

}
