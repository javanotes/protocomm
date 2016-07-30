package com.reactiva.emulator.xcomm.sh;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smsnow.protocol.IType;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
@Sharable
public class RequestProcessorHandler extends MessageToMessageDecoder<IType> {

	private static final Logger log = LoggerFactory.getLogger(RequestProcessorHandler.class);
	/**
	 * Pass all helper class instances, if reqd. Singleton scoped.
	 */
	public RequestProcessorHandler()
	{
		
	}
	private RequestDispatcher handlers;
	public RequestProcessorHandler(RequestDispatcher handlers) {
		this.handlers = handlers;
	}
	/**
	 * Process the request using some service class.
	 * @param request
	 * @return
	 */
	protected IType doProcess(IType request) throws Exception
	{
		try {
			return handlers.service(request);
		} catch (Exception e) {
			log.error("-- Handler exception --", e);
		}
		return new IType() {
			
			@Override
			public short code() {
				return -1;
			}
		};
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, IType msg, List<Object> out) throws Exception {
		IType r = doProcess(msg);
		out.add(r);
		
	}

}
