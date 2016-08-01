package com.smsnow.adaptation.server.pipe;

import java.io.Serializable;
import java.util.List;

import com.smsnow.adaptation.server.RequestHandler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
@Sharable
public class RequestProcessorHandler extends MessageToMessageDecoder<Serializable> {

	/**
	 * Pass all helper class instances, if reqd. Singleton scoped.
	 */
	public RequestProcessorHandler()
	{
		
	}
	private RequestHandler handler;
	public RequestProcessorHandler(RequestHandler handlers) {
		this.handler = handlers;
	}
	/**
	 * Process the request using some service class.
	 * @param request
	 * @return
	 */
	protected Serializable doProcess(Serializable request) throws Exception
	{
		return handler.process(request);
		
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, Serializable msg, List<Object> out) throws Exception {
		Serializable r = doProcess(msg);
		out.add(r);
		
	}

}
