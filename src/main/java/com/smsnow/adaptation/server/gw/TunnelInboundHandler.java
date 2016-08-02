package com.smsnow.adaptation.server.gw;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.smsnow.adaptation.loadbal.Balancer;
import com.smsnow.adaptation.loadbal.BalancingStrategy;
import com.smsnow.adaptation.loadbal.Target.Algorithm;
import com.smsnow.adaptation.server.Utils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;
/**
 * The handler class to use the server as a proxy gateway.
 * @author esutdal
 *
 */
@Sharable
public class TunnelInboundHandler extends ChannelDuplexHandler {

	private static final Logger log = LoggerFactory.getLogger(TunnelInboundHandler.class);
	
	private final OutboundEndpoints endpoints;
	private final int size;
	
	
	/**
	 * 
	 * @param targets
	 * @param eventGroup 
	 */
	public TunnelInboundHandler(List<OutboundEndpoint> targets, EventLoopGroup eventGroup) {
		Assert.notNull(targets, "Target destination is null");
		Assert.notEmpty(targets, "Target destination is empty");
		
		endpoints = new OutboundEndpoints(targets, Algorithm.ROUNDROBIN);
		size = endpoints.size();
		
		//TODO: need to change balancing algorithm based on thread local
		balancer = Balancer.getBalancer(Collections.synchronizedList(targets), Algorithm.ROUNDROBIN);
		
		this.eventGroups = eventGroup;
		
		log.info("Using balancing strategy: "+balancer.strategy());
	}
	final ConcurrentMap<String, OEMetrics> activeSessions = new ConcurrentHashMap<>();
	final ConcurrentMap<String, Object> reconnectingHosts = new ConcurrentHashMap<>();
	private final Object reconnectingHostsVal = new Object();
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
		int n = activeSessions.get(client.remoteAddress().toString()).incrementAndGet();
		if (log.isDebugEnabled()) {
			log.debug(activeSessions.get(client.remoteAddress().toString()).toString());
		}
		return n;
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
			int n = activeSessions.get(client.remoteAddress().toString()).decrementAndGet();
			if (log.isDebugEnabled()) {
				log.debug(activeSessions.get(client.remoteAddress().toString()).toString());
			}
			return n;
		}
		return 0;
	}
	
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	log.debug("Remote client "+ctx.channel().remoteAddress() + " was disconnected");
    	decrConnection(ctx.channel());
    	log.info("Channel inactive "+ctx.channel().remoteAddress()+" SessionID# "+sessions.remove(ctx.channel().remoteAddress().toString()));
    }
    
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    	if (activeSessions.containsKey(ctx.channel().remoteAddress().toString())) {
			activeSessions.get(ctx.channel().remoteAddress().toString()).onResponseReceived();
		}
		super.write(ctx, msg, promise);
        log.debug(ctx.channel().remoteAddress()+" :: RESPONSE PREPARED");
        log.info("Channel write "+ctx.channel().remoteAddress()+" SessionID# "+sessions.get(ctx.channel().remoteAddress().toString()));
    }
    
    private OutboundEndpoint getNext()
    {
    	OutboundEndpoint target = balancer.getNext();
    	return endpoints.get(target);
    }
    
    private void onTargetReady(ChannelHandlerContext ctx, Object msg, OutboundEndpoint target)
    {
    	incrConnection(ctx.channel(), target);
		activeSessions.get(ctx.channel().remoteAddress().toString()).onRequestSent();
		target.write(ctx, msg);
		endReconnectionAttempt(target.toString());
		log.debug(ctx.channel().remoteAddress()+" :: REQUEST PREPARED");
		log.info("Channel read "+ctx.channel().remoteAddress()+" SessionID# "+sessions.get(ctx.channel().remoteAddress().toString()));
    }
	@Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		int c = 0;
		boolean written = false;
		while (c++ < size) {
			OutboundEndpoint target = getNext();
			try 
			{
				if(!target.isReady())
				{
					if (beginReconnectionAttempt(target.toString())) {
						if (target.isUnInitiated()) 
						{
							log.info(">> Initiating tunnel to "+target.toString()+". This can take some time");
							target.setTunnelInHandler(this);
							initTunnel(target, 30, TimeUnit.SECONDS);
							if(target.isReady())
							{
								onTargetReady(ctx, msg, target);
								written = true;
								break;
							}
						}
						else
						{
							initTunnel(target, 0, null);
						}
					}
					else
					{
						log.debug("Target "+target+" is not ready, but did not add to init tunnel");
					}
				}
				else
				{
					onTargetReady(ctx, msg, target);
					written = true;
					break;
				}
			} finally {
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
    	log.warn("#Trace#", e);
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
    	
    	final TunnelOutboundHandler tunnelOutHdlr = new TunnelOutboundHandler();
    	
		// Start the connection attempt.
		Bootstrap b = new Bootstrap();
		b
		.group(eventGroups)
		.channel(NioSocketChannel.class)
		.handler(new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(executors, tunnelOutHdlr);
				
			}
		})
		.option(ChannelOption.AUTO_READ, false)
		.option(ChannelOption.SO_KEEPALIVE, true)
		.option(ChannelOption.SO_REUSEADDR, true)
		;
		
		ChannelFuture f = b.connect(h.getHost(), h.getPort());
		log.info("Negotiating destination "+h);
		
		h.associate(f);

		if (await > 0) {
			if (!h.awaitReady(await, unit)) {
				log.warn("<< " + h + " not reachable now. Will retry connection on subsequent channel executions >>");
			}
			;
		}
		h.setTunnelOutHandler(tunnelOutHdlr);
	}
    private BalancingStrategy<OutboundEndpoint> balancer;
    private ConcurrentMap<String, UUID> sessions = new ConcurrentHashMap<>();
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		
		boolean interrupt = false;
		sessions.putIfAbsent(ctx.channel().remoteAddress().toString(), UUID.randomUUID());
		log.info("Channel active "+ctx.channel().remoteAddress()+" SessionID# "+sessions.get(ctx.channel().remoteAddress().toString()));
		
		ctx.read();
						
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
	
	/**
	 * 
	 * @param host
	 * @return
	 */
	boolean beginReconnectionAttempt(String host) {
		return reconnectingHosts.putIfAbsent(host, reconnectingHostsVal) == null;
		
	}
	private final EventLoopGroup eventGroups;
	private EventExecutorGroup executors;
	public void setExecutorGroup(EventExecutorGroup executor) {
		this.executors = executor;
	}
	
}
