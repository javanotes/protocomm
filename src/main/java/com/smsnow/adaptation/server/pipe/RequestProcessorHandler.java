package com.smsnow.adaptation.server.pipe;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smsnow.adaptation.dto.ITOCRequest;
import com.smsnow.adaptation.server.RequestHandler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
@Sharable
public class RequestProcessorHandler extends MessageToMessageDecoder<ITOCRequest> {

	private static final Logger log = LoggerFactory.getLogger(RequestProcessorHandler.class);
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
	private Serializable doProcess(Serializable request) throws Exception
	{
		log.info("[ECHO] Request processing .."+request);
		return request;
		
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ITOCRequest msg, List<Object> out) throws Exception {
		Serializable r = doProcess(msg);
		out.add(r);
		
	}

}
