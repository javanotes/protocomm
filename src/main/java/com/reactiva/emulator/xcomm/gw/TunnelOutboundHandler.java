package com.reactiva.emulator.xcomm.gw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reactiva.emulator.xcomm.Utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

class TunnelOutboundHandler extends ChannelInboundHandlerAdapter {

	private static Logger log = LoggerFactory.getLogger(TunnelOutboundHandler.class);
	private Channel clientChannel;
	/**
	 * 
	 * @param inboundChannel
	 */
	public TunnelOutboundHandler(Channel inboundChannel) {
		this.setClientChannel(inboundChannel);
	}
	/**
	 * 
	 */
	public TunnelOutboundHandler() {
		
	}
	
	@Override
    public void channelRead(final ChannelHandlerContext hostCtx, Object msg) throws Exception {
		log.debug("REVERSE TUNNEL WRITE => server 2 client");
		getClientChannel().writeAndFlush(msg).addListener(new ChannelFutureListener() {
	        @Override
	        public void operationComplete(ChannelFuture future) {
	        	hostCtx.channel().read();
	        	log.debug("REVERSE TUNNEL WRITE complete");
	        }
		});
    }

	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
            throws Exception {
        log.warn("<Trace suppressed> Closing Outbound (target) channel to "+ctx.channel().remoteAddress());
        log.debug("", e);
        Utils.closeOnIOErr(ctx.channel());
    }
	private Channel getClientChannel() {
		return clientChannel;
	}
	/**
	 * Set and request read
	 * @param clientChannel
	 */
	public void setClientChannel(Channel clientChannel) {
		this.clientChannel = clientChannel;
		this.clientChannel.read();
	}
}
