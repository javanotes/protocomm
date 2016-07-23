package com.reactiva.emulator.xcomm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.reactiva.emulator.xcomm.Config.HostAndPort;
import com.reactiva.emulator.xcomm.gw.OutboundEndpoint;
import com.reactiva.emulator.xcomm.gw.TunnelInboundHandler;
import com.reactiva.emulator.xcomm.sh.BasicChannelHandler;
import com.reactiva.emulator.xcomm.sh.RequestConvertorHandlerFactory;
import com.reactiva.emulator.xcomm.sh.RequestProcessorHandler;
import com.reactiva.emulator.xcomm.sh.ResponseConvertorHandler;
import com.reactiva.emulator.xcomm.sh.TerminalHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.RejectedExecutionHandler;

class TCPConnector implements Runnable{
	private BasicChannelHandler bch;
	private DefaultEventExecutorGroup executor;
	/**
	 * 
	 * @param ch
	 * @throws Exception
	 */
	protected void serverHandlers(SocketChannel ch) throws Exception
	{
		
		Assert.notNull(config);
		/**
		 * The maxFrameLength can be set as per the protocol design (if defined). That
		 * would enable rejection of too long messages. Right now, this an arbitrary number
		 * assuming the actual message size would be much lesser than that.
		 * TODO: make LengthFieldBasedFrameDecoder configurable?
		 */
		ch.pipeline().addLast(executor, new LengthFieldBasedFrameDecoder(config.protoLenMax, config.protoLenOffset, config.protoLenBytes, Math.negateExact(config.protoLenBytes), 0));
		ch.pipeline().addLast(executor, decoder.getObject(), processor, encoder);
		ch.pipeline().addLast(executor, terminal);
		
		//ch.pipeline().addLast(bch);
	}
	/**
	 * 
	 * @param ch
	 * @throws Exception
	 */
	protected void proxyHandlers(SocketChannel ch) throws Exception
	{
		ch.pipeline().addLast(balancer);
	}
	
	/**
	 * 
	 * @author esutdal
	 *
	 */
	private final class Handlers extends ChannelInitializer<SocketChannel> {
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
	private int port, ioThreads, eventThreads;
	
	protected RequestConvertorHandlerFactory decoder;
	protected RequestProcessorHandler processor;
	protected ResponseConvertorHandler encoder;
	protected TerminalHandler terminal;
	
	private final boolean proxy;
	private Config config;
	private TunnelInboundHandler balancer;
	/**
	 * A TCP connector which can act as a server or proxy.
	 * @param port
	 * @param ioThreadCount
	 */
	public TCPConnector(int port, int ioThreadCount, int execThreadCount, boolean proxy) {
		this.port = port;
		ioThreads = ioThreadCount;
		eventThreads = execThreadCount;
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
			
			//int poolSize = targets.getMaxpool().containsKey(h) ? targets.getMaxpool().get(h) : 1;
			
			if(p.contains(","))
			{
				for(String s : p.split(","))
				{
					try {
						OutboundEndpoint ep = new OutboundEndpoint(h, Integer.valueOf(s));
						ep.setMaxConnections(1);
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
					ep.setMaxConnections(1);
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
	private ServerBootstrap server;
	private NioEventLoopGroup eventLoop, bossLoop;
	/**
	 * 
	 */
	private void open()
	{
		eventLoop = new NioEventLoopGroup(ioThreads);
		bossLoop = new NioEventLoopGroup(1);
		if (proxy) {
			balancer = new TunnelInboundHandler(loadTargets());
		}
		
		if (proxy) {
			balancer.setEventLoops(eventLoop);
		}
		executor = new DefaultEventExecutorGroup(1, new ThreadFactory() {
			int n=1;
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "xcomm-nio-"+(n++));
				return t;
			}
		})
		{
			@Override
		    protected EventExecutor newChild(Executor executor, Object... args) throws Exception {
				return new ConcurrentEventExecutor(this, executor, (Integer) args[0], (RejectedExecutionHandler) args[1], eventThreads);
				//return new DefaultEventExecutor(this, executor, (Integer) args[0], (RejectedExecutionHandler) args[1]);
		    }
		};
		
		server = new ServerBootstrap()
				.group(bossLoop, eventLoop)
				.channel(NioServerSocketChannel.class)
				.childHandler(new Handlers())
				.option(ChannelOption.SO_BACKLOG, 256)    
	            .childOption(ChannelOption.SO_KEEPALIVE, true)
	            ;
	}
	
	@Override
	public void run() {
		//TODO: can be implemented for some monitoring stuff.
		while(running)
		{
			doTask();
		}

	}
	
	void stopMonitor()
	{
		running = false;
	}
	private void doTask() {
		log.debug("--Monitor task run --");
		synchronized (this) {
			try {
				wait(5000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	private volatile boolean running = false;
	/**
	 * 
	 */
	public void stopServer() {
		try {
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		eventLoop.shutdownGracefully();
		bossLoop.shutdownGracefully();
		if(executor != null)
			executor.shutdownGracefully();
		log.info("Stopped transport on port "+port);
	}

	private ChannelFuture future;
	/**
	 * @throws InterruptedException 
	 * 
	 */
	public void startServer() throws InterruptedException {
		open();
		future = server.bind(port).sync();
		log.info("Started TCP transport on port "+port + " in "+(proxy ? "PROXY" : "SERVER") + " mode");
		running = true;
	}
	@Autowired
	private HostAndPort targets;
	/**
	 * @deprecated
	 * @param bch
	 */
	public void addHandler(BasicChannelHandler bch) {
		this.bch = bch;
	}
	public Config getConfig() {
		return config;
	}
	public void setConfig(Config config) {
		this.config = config;
	}
	

}
