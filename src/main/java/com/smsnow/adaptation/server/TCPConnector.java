package com.smsnow.adaptation.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.smsnow.adaptation.protocol.LengthBasedCodec;
import com.smsnow.adaptation.protocol.itoc.ITOCCodecWrapper;
import com.smsnow.adaptation.server.Config.HostAndPort;
import com.smsnow.adaptation.server.gw.OutboundEndpoint;
import com.smsnow.adaptation.server.gw.TunnelInboundHandler;
import com.smsnow.adaptation.server.pipe.RequestConvertorHandler;
import com.smsnow.adaptation.server.pipe.RequestProcessorHandler;
import com.smsnow.adaptation.server.pipe.RequestProcessorHandlerAsync;
import com.smsnow.adaptation.server.pipe.ResponseConvertorHandler;
import com.smsnow.adaptation.server.pipe.TerminalHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.RejectedExecutionHandler;
/**
 * The Netty server listener class that registers channel handler adapters.
 * @author esutdal
 *
 */
class TCPConnector implements Runnable{
	private EventExecutorGroup eventExecutors, procExecutors;
	private ITOCCodecWrapper codecHandler;
	public LengthBasedCodec getCodecHandler() {
		return codecHandler;
	}
	public void setCodecHandler(ITOCCodecWrapper codecHandler) {
		this.codecHandler = codecHandler;
	}
	private RequestHandler requestHandler;
	public RequestHandler getRequestHandler() {
		return requestHandler;
	}
	public void setRequestHandler(RequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}
	/**
	 * 
	 * @param ch
	 * @throws Exception
	 */
	protected void serverHandlers(SocketChannel ch) throws Exception
	{
		
		Assert.notNull(config);
		Assert.notNull(codecHandler);
		Assert.notNull(requestHandler);
		/**
		 * The maxFrameLength can be set as per the protocol design (if defined). That
		 * would enable rejection of too long messages. Right now, this an arbitrary number
		 * assuming the actual message size would be much lesser than that.
		 * TODO: make LengthFieldBasedFrameDecoder configurable?
		 */
		ch.pipeline().addLast(eventExecutors, new LengthFieldBasedFrameDecoder(config.protoLenMax, config.protoLenOffset, 
				config.protoLenBytes, Math.negateExact(config.protoLenBytes), 0));
		
		//ch.pipeline().addLast(executor, new LengthFieldBasedFrameDecoder(config.protoLenMax, config.protoLenOffset, config.protoLenBytes));
		
		ch.pipeline().addLast(eventExecutors, new RequestConvertorHandler(codecHandler, requestHandler, config.useByteBuf));
		//ch.pipeline().addLast(concExecutor, processor);
		ch.pipeline().addLast(procExecutors, processorAsync);
		ch.pipeline().addLast(eventExecutors, encoder);
		ch.pipeline().addLast(eventExecutors, terminal);
		
	}
	/**
	 * 
	 * @param ch
	 * @throws Exception
	 */
	protected void proxyHandlers(SocketChannel ch) throws Exception
	{
		if (tunnelHandler == null) {
			synchronized (TCPConnector.class) {
				if (tunnelHandler == null) {
					tunnelHandler = new TunnelInboundHandler(loadTargets(), eventLoop);
					tunnelHandler.setOutboundExecutor(eventExecutors);
					tunnelHandler.setConfig(config);
				}
			}
		}
		ch.pipeline().addLast(eventExecutors, new LengthFieldBasedFrameDecoder(config.protoLenMax, config.protoLenOffset, config.protoLenBytes), tunnelHandler);
	}
	
	/**
	 * Strategy class for loading appropriate handlers.
	 * @author esutdal
	 *
	 */
	private final class HandlerInitializer extends ChannelInitializer<SocketChannel> {
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			
			if(proxy)
			{
				proxyHandlers(ch);
			}
			else
			{
				serverHandlers(ch);
			}
			
		}
	}
	private static Logger log = LoggerFactory.getLogger(TCPConnector.class);
	private int port, ioThreads, execThreads;
	
	protected RequestProcessorHandler processor;
	protected ResponseConvertorHandler encoder;
	protected TerminalHandler terminal;
	
	private final boolean proxy;
	private Config config;
	private TunnelInboundHandler tunnelHandler;
	/**
	 * A TCP connector which can act as a server or proxy.
	 * @param port
	 * @param ioThreadCount
	 */
	public TCPConnector(int port, int ioThreadCount, int execThreadCount, boolean proxy) {
		this.port = port;
		ioThreads = ioThreadCount;
		execThreads = execThreadCount;
		this.proxy = proxy;
		
	}
	/**
	 * 
	 * @return
	 */
	protected List<OutboundEndpoint> loadTargets() {
		Assert.notNull(targets, "Gateway destinations not found");
		Assert.notNull(targets.getTarget(), "Gateway destinations not found");
		List<OutboundEndpoint> out = new ArrayList<>();
		
		for(Entry<String, String> e : targets.getTarget().entrySet())
		{
			String h = e.getKey();
			String p = e.getValue();
			
			if(p.contains(","))
			{
				for(String s : p.split(","))
				{
					try {
						OutboundEndpoint ep = new OutboundEndpoint(h, Integer.valueOf(s));
						out.add(ep);
					} catch (NumberFormatException e1) {
						throw new IllegalArgumentException("Unparseable port "+s);
					}
				}
			}
			else
			{
				try {
					OutboundEndpoint ep = new OutboundEndpoint(h, Integer.valueOf(p));
					out.add(ep);
				} catch (NumberFormatException e1) {
					throw new IllegalArgumentException("Unparseable port "+p);
				}
			}
		}
		if (!out.isEmpty()) {
			log.info("Gateway destinations loaded: " + out);
		}
		else
		{
			throw new IllegalArgumentException("Gateway destinations not found");
		}
		return out;
	}
	/**
	 * TCP connector acting as server.
	 * @param port
	 * @param workerThreadCount
	 */
	public TCPConnector(int port, int workerThreadCount) {
		this(port, workerThreadCount, Runtime.getRuntime().availableProcessors(), false);
	}
	/**
	 * 
	 * @param port
	 * @param workerThreadCount
	 * @param proxyMode
	 */
	public TCPConnector(int port, int workerThreadCount, boolean proxyMode) {
		this(port, 1, workerThreadCount, proxyMode);
	}
	/**
	 * 
	 */
	private void initIOThreads()
	{
		eventLoop = new NioEventLoopGroup(ioThreads, new ThreadFactory() {
			int n = 1;

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "xcomm-io-" + (n++));
				return t;
			}
		});
		bossLoop = new NioEventLoopGroup(1, new ThreadFactory() {
			int n = 1;

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "xcomm-accept-" + (n++));
				return t;
			}
		});
	}
	/**
	 * 
	 */
	private void initExecThreads()
	{
		eventExecutors = new DefaultEventExecutorGroup(config.eventThreadCount, new ThreadFactory() {
			int n = 1;
			@Override
			public Thread newThread(Runnable arg0) {
				Thread t = new Thread(arg0, "xcomm-event-"+(n++));
				return t;
			}
		});
		
		procExecutors = new DefaultEventExecutorGroup(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable arg0) {
				Thread t = new Thread(arg0, "xcomm-execgrp");
				return t;
			}
		}) 
		{
			@Override
			protected EventExecutor newChild(Executor executor, Object... args) throws Exception {
				return new ConcurrentEventExecutor(this, executor, (Integer) args[0],
						(RejectedExecutionHandler) args[1], execThreads);
				
			}
		};
	}
	private ServerBootstrap server;
	private NioEventLoopGroup eventLoop, bossLoop;
	/**
	 * Setup the transport channel
	 */
	private void setup()
	{
		initIOThreads();
		initExecThreads();
				
		server = new ServerBootstrap()
				.group(bossLoop, eventLoop)
				.channel(NioServerSocketChannel.class)
				.childHandler(new HandlerInitializer())
				.option(ChannelOption.SO_BACKLOG, 256)    
	            ;
	}
	
	@Override
	public void run() {
		//TODO: can be implemented for some monitoring stuff.
		while(running)
		{
			try {
				doMonitorTask();
			} finally {
			}
		}

	}
	
	void stopMonitor()
	{
		running = false;
	}
	protected void doMonitorTask() {
		log.debug("--Monitor task run --");
		
	}
	private volatile boolean running = false;
	/**
	 * 
	 */
	public void stopServer() {
		future.channel().closeFuture().syncUninterruptibly();
		eventLoop.shutdownGracefully().syncUninterruptibly();
		bossLoop.shutdownGracefully().syncUninterruptibly();
		if(eventExecutors != null)
			eventExecutors.shutdownGracefully().syncUninterruptibly();
		if(procExecutors != null)
			procExecutors.shutdownGracefully().syncUninterruptibly();
		log.info("Stopped transport on port "+port);
	}

	private ChannelFuture future;
	/**
	 * @throws InterruptedException 
	 * 
	 */
	public void startServer() throws InterruptedException {
		setup();
		future = server.bind(port).sync();
		log.info("Started TCP transport on port "+port + " in "+(proxy ? "PROXY" : "SERVER") + " mode");
		running = true;
	}
	@Autowired
	private HostAndPort targets;
	RequestProcessorHandlerAsync processorAsync;
	
	public Config getConfig() {
		return config;
	}
	public void setConfig(Config config) {
		this.config = config;
	}
	

}
