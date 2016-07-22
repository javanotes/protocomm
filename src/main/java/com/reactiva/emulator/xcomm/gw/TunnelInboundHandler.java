package com.reactiva.emulator.xcomm.gw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.reactiva.emulator.xcomm.Utils;
import com.reactiva.emulator.xcomm.gw.bal.Balancer;
import com.reactiva.emulator.xcomm.gw.bal.BalancingStrategy;
import com.reactiva.emulator.xcomm.gw.bal.Target.Algorithm;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
/**
 * The handler class to use the server as a proxy gateway.
 * @author esutdal
 *
 */
@Sharable
public class TunnelInboundHandler extends ChannelDuplexHandler {

	private static final Logger log = LoggerFactory.getLogger(TunnelInboundHandler.class);
	
	private final List<OutboundEndpoint> targets;
	private final int size;
	private NioEventLoopGroup selectorLoop = new NioEventLoopGroup();
	/**
	 * 
	 * @param targets
	 */
	public TunnelInboundHandler(List<OutboundEndpoint> targets) {
		
		Assert.notNull(targets, "Target destination is null");
		Assert.notEmpty(targets, "Target destination is empty");
		this.targets = new ArrayList<>(targets);
		size = this.targets.size();
		
		balancer = Balancer.getBalancer(Collections.synchronizedList(this.targets), Algorithm.ROUNDROBIN);
		
		log.info("Using balancing strategy: "+balancer.strategy());
	}
	final ConcurrentMap<String, OEMetrics> activeSessions = new ConcurrentHashMap<>();
	final ConcurrentSkipListSet<String> reconnectingHosts = new ConcurrentSkipListSet<>();
	/**
	 * 
	 * @param client
	 * @param host
	 * @return
	 */
	private int incrConnection(Channel client, OutboundEndpoint host)
	{
		if(!activeSessions.containsKey(client.remoteAddress().toString()))
		{
			activeSessions.putIfAbsent(client.remoteAddress().toString(), new OEMetrics(host));
		}
		return activeSessions.get(client.remoteAddress().toString()).incrementAndGet();
	}
	/**
	 * 
	 * @param client
	 * @return
	 */
	private int decrConnection(Channel client)
	{
		if(activeSessions.containsKey(client.remoteAddress().toString()))
		{
			return activeSessions.get(client.remoteAddress().toString()).decrementAndGet();
		}
		return 0;
	}
	
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	log.debug("Remote client "+ctx.channel().remoteAddress() + " was disconnected");
    	decrConnection(ctx.channel());
    }
    
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    	if (activeSessions.containsKey(ctx.channel().remoteAddress().toString())) {
			activeSessions.get(ctx.channel().remoteAddress().toString()).onResponseReceived();
		}
		super.write(ctx, msg, promise);
        log.debug(ctx.channel().remoteAddress()+" :: RESPONSE PREPARED");
    }
    
	@Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		int c = 0;
		boolean written = false;
		while (c++ < size) {
			OutboundEndpoint target = balancer.getNext();
			try 
			{
				if(!target.isReady())
				{
					if (reconnectingHosts.add(target.toString())) {
						initTunnel(target, 0, null);
					}
					else
					{
						log.debug("Target "+target+" is not ready, but did not add to init tunnel");
					}
				}
				else
				{
					incrConnection(ctx.channel(), target);
					activeSessions.get(ctx.channel().remoteAddress().toString()).onRequestSent();
					target.write(ctx, msg);
					reconnectingHosts.remove(target.toString());
					log.debug(ctx.channel().remoteAddress()+" :: REQUEST PREPARED");
					written = true;
					break;
				}
			} finally {
				//targets.addLast(target);
			} 
		}
		if(!written)
		{
			log.warn("* Force closing client connection from "+ctx.channel().remoteAddress()+". No host available *");
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
    	log.info("<< Negotiating target destination "+h+" >>");
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
		
		//FixedChannelPool pool = new 
		ChannelFuture f = b.connect(h.getHost(), h.getPort());
		
		h.associate(f);

		if (await > 0) {
			if (!h.awaitReady(await, unit)) {
				log.warn("<< " + h + " not reachable now. Will retry connection on subsequent channel executions >>");
			}
			;
		}
		h.setTunnelOutHandler(tunnelOutHdlr);
		h.setInitiated(true);
	}
    private BalancingStrategy<OutboundEndpoint> balancer;
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		
		boolean interrupt = false;
		for(OutboundEndpoint h : targets)
		{
			if (!h.isInitiated()) {
				try 
				{
					h.tunnelInHandler = this;
					initTunnel(h, 1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					interrupt = true;
				} 
			}
		}
				
		if(interrupt)
			Thread.currentThread().interrupt();
	}
	/**
	 * 
	 * @param host
	 */
	void endReconnectionAttempt(String host) {
		reconnectingHosts.remove(host);
		
	}
}
