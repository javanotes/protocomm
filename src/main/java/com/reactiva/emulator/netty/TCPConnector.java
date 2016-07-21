package com.reactiva.emulator.netty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.reactiva.emulator.netty.Config.HostAndPort;
import com.reactiva.emulator.netty.gw.CircularBalancingHandler;
import com.reactiva.emulator.netty.gw.OutboundEndpoint;
import com.reactiva.emulator.netty.sh.BasicChannelHandler;
import com.reactiva.emulator.netty.sh.RequestConvertorHandlerFactory;
import com.reactiva.emulator.netty.sh.RequestProcessorHandler;
import com.reactiva.emulator.netty.sh.ResponseConvertorHandler;
import com.reactiva.emulator.netty.sh.TerminalHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

class TCPConnector implements Runnable{
	private BasicChannelHandler bch;
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
		ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(config.protoLenMax, config.protoLenOffset, config.protoLenBytes, Math.negateExact(config.protoLenBytes), 0));
		ch.pipeline().addLast(decoder.getObject(), processor, encoder);
		ch.pipeline().addLast(terminal);
		
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
	private int port, wThreads;
	
	protected RequestConvertorHandlerFactory decoder;
	protected RequestProcessorHandler processor;
	protected ResponseConvertorHandler encoder;
	protected TerminalHandler terminal;
	
	private final boolean proxy;
	private Config config;
	private CircularBalancingHandler balancer;
	/**
	 * A TCP connector which can act as a server or proxy.
	 * @param port
	 * @param workerThreadCount
	 */
	public TCPConnector(int port, int workerThreadCount, boolean proxy) {
		this.port = port;
		wThreads = workerThreadCount;
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
						out.add(new OutboundEndpoint(h, Integer.valueOf(s)));
					} catch (NumberFormatException e1) {
						throw new IllegalArgumentException("Unparseable port "+s);
					}
				}
			}
			else
			{
				try {
					out.add(new OutboundEndpoint(h, Integer.valueOf(p)));
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
		this(port, workerThreadCount, false);
	}
	private ServerBootstrap server;
	private NioEventLoopGroup boss, worker;
	/**
	 * 
	 */
	private void open()
	{
		if (proxy) {
			balancer = new CircularBalancingHandler(loadTargets());
		}
		
		boss = new NioEventLoopGroup(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "TCPAccept");
				return t;
			}
		});
		worker = new NioEventLoopGroup(wThreads, new ThreadFactory() {
			int n=0;
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "TCPConn-"+(n++));
				return t;
			}
		});
		server = new ServerBootstrap()
				.group(boss, worker)
				.channel(NioServerSocketChannel.class)
				.childHandler(new Handlers())
				.option(ChannelOption.SO_BACKLOG, 128)    
	            .childOption(ChannelOption.SO_KEEPALIVE, true)
	            ;
	}
	
	@Override
	public void run() {
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
		worker.shutdownGracefully();
		boss.shutdownGracefully();
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
