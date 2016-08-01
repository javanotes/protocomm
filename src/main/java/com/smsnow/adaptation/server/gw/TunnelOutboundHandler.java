package com.smsnow.adaptation.server.gw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smsnow.adaptation.server.Utils;

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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }
	
	@Override
    public void channelRead(final ChannelHandlerContext hostCtx, Object msg) throws Exception {
		log.debug("REVERSE TUNNEL WRITE => server 2 client");
		getClientChannel().writeAndFlush(msg).addListener(new ChannelFutureListener() {
	        @Override
	        public void operationComplete(ChannelFuture future) {
	        	hostCtx.read();
	        	log.debug("REVERSE TUNNEL WRITE complete");
	        }
		});
    }

	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
            throws Exception {
        log.warn("<Trace suppressed> Closing Outbound (target) channel to "+ctx.channel().remoteAddress());
        log.debug("#Trace#", e);
        Utils.closeOnIOErr(ctx.channel());
    }
	/**
	 * 
	 * @return
	 */
	private synchronized Channel getClientChannel() {
		return clientChannel;
	}
	/**
	 * Set and request read
	 * @param clientChannel
	 */
	public synchronized void setClientChannel(Channel clientChannel) {
		this.clientChannel = clientChannel;
	}
}