package com.reactiva.emulator.netty.gw;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.reactiva.emulator.netty.Utils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
/**
 * The handler class to use the server as a proxy gateway.
 * @author esutdal
 *
 */
@Sharable
public class CircularBalancingHandler extends ChannelInboundHandlerAdapter {

	private static final Logger log = LoggerFactory.getLogger(CircularBalancingHandler.class);
	
	private final LinkedList<OutboundEndpoint> targets;
	private final int size;
	private NioEventLoopGroup selectorLoop = new NioEventLoopGroup();
	/**
	 * 
	 * @param targets
	 */
	public CircularBalancingHandler(List<OutboundEndpoint> targets) {
		this.targets = new LinkedList<>(targets);
		Assert.notNull(targets, "Target destination is null");
		Assert.notEmpty(targets, "Target destination is empty");
		size = targets.size();
	}
	final ConcurrentMap<String, AtomicInteger> activeSessions = new ConcurrentHashMap<>();
	final ConcurrentSkipListSet<String> reconnectHosts = new ConcurrentSkipListSet<>();
	
	private int incrConnection(Channel target)
	{
		if(!activeSessions.containsKey(target.remoteAddress().toString()))
		{
			activeSessions.putIfAbsent(target.remoteAddress().toString(), new AtomicInteger(0));
		}
		return activeSessions.get(target.remoteAddress().toString()).incrementAndGet();
	}
	private int decrConnection(Channel target)
	{
		if(activeSessions.containsKey(target.remoteAddress().toString()))
		{
			return activeSessions.get(target.remoteAddress().toString()).decrementAndGet();
		}
		return 0;
	}
	
	@Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		decrConnection(ctx.channel());
		log.debug(ctx.channel().remoteAddress() + " was unregistered");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	log.debug(ctx.channel().remoteAddress() + " was inactivated");
    }
    
	@Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		int c = 0;
		boolean written = false;
		while (c++ < size) {
			OutboundEndpoint target = targets.pollFirst();
			try 
			{
				if(!target.isReady())
				{
					if (reconnectHosts.add(target.toString())) {
						initTunnel(target, 0, null);
					}
					
				}
				else
				{
					incrConnection(ctx.channel());
					reconnectHosts.remove(target.toString());
					target.write(ctx, msg);
					written = true;
					break;
				}
			} finally {
				targets.addLast(target);
			} 
		}
		if(!written)
		{
			log.warn("* Force closing client connection from "+ctx.channel().remoteAddress()+" on no destination available *");
			Utils.closeOnNoHost(ctx.channel());
		}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
            throws Exception {
    	log.warn("<Trace suppressed> Closing Inbound (source) channel from "+ctx.channel().remoteAddress());
    	log.debug("", e);
    	Utils.closeOnIOErr(ctx.channel());
    }
    /**
     * Creates a new socket connection to a target destination.
     * @param h
     * @param await
     * @param unit
     * @throws InterruptedException
     */
    protected void initTunnel(OutboundEndpoint h, long await, TimeUnit unit) throws InterruptedException
    {
    	TunnelOutboundHandler tunnelOutHdlr = new TunnelOutboundHandler();
		// Start the connection attempt.
		Bootstrap b = new Bootstrap();
		b
		.group(selectorLoop)
		.channel(NioSocketChannel.class)
		.handler(tunnelOutHdlr)
		.option(ChannelOption.AUTO_READ, true)
		.option(ChannelOption.SO_KEEPALIVE, true)
		.option(ChannelOption.SO_REUSEADDR, true)
		//.option(ChannelOption.SO_LINGER, 2)
		;
		log.info("Acquiring target destination => "+h);
		ChannelFuture f = b.connect(h.getHost(), h.getPort());
		
		h.associate(f);

		if (await > 0) {
			if (!h.awaitReady(await, unit)) {
				log.warn("* " + h + " not reachable now. Will retry connection on subsequent channel executions *");
			}
			;
		}
		h.setTunnelOutHandler(tunnelOutHdlr);
		h.setInitiated(true);
	}
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		
		boolean interrupt = false;
		for(OutboundEndpoint h : targets)
		{
			if (!h.isInitiated()) {
				try {
					h.setHostConnSet(this.reconnectHosts);
					initTunnel(h, 1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					interrupt = true;
				} 
			}
		}
		if(interrupt)
			Thread.currentThread().interrupt();
	}
}
