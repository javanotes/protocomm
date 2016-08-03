package com.smsnow.adaptation.server.gw;

import java.util.concurrent.atomic.AtomicBoolean;

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
		//notifyOnOutboundRead();
    }

	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
            throws Exception {
        log.warn("<Trace suppressed> Closing Outbound (target) channel to "+ctx.channel().remoteAddress());
        log.debug("#Trace#", e);
        Utils.closeOnIOErr(ctx.channel());
    }
	private final AtomicBoolean inProc = new AtomicBoolean();
	/**
	 * 
	 * @return
	 */
	private Channel getClientChannel() {
		return clientChannel;
	}
	private void notifyOnOutboundRead()
	{
		synchronized (inProc) {
    		inProc.compareAndSet(true, false);
    		inProc.notifyAll();
		}
	}
	private void waitOnOutboundRead()
	{
		boolean intr = false;
		synchronized (inProc) {
			while(!inProc.compareAndSet(false, true))
				try {
					inProc.wait();
				} catch (InterruptedException e) {
					intr = true;
				}
		}
		if(intr)
			Thread.currentThread().interrupt();
	}
	/**
	 * Set and request read
	 * @param clientChannel
	 */
	public void setClientChannel(Channel clientChannel) {
		//waitOnOutboundRead();
		this.clientChannel = clientChannel;
	}
}
