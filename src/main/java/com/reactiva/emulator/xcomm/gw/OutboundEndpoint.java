package com.reactiva.emulator.xcomm.gw;

import java.io.Closeable;
import java.math.BigDecimal;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reactiva.emulator.xcomm.gw.bal.Target;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
/**
 * 
 * @author esutdal
 *
 */
public class OutboundEndpoint implements Closeable, Target, Endpoint, ChannelPoolHandler{

	/*
	 * 	The possible state transitions for TCP based child and client channels are:
		OPEN -> ( BOUND -> ( CONNECTED -> DISCONNECTED )* -> UNBOUND )* -> CLOSE
		
		The possible state transitions for TCP based server channels are:
		OPEN -> ( BOUND -> UNBOUND )* -> CLOSE


	 */
	
	static final Logger log = LoggerFactory.getLogger(OutboundEndpoint.class);
	// constants ------------------------------------------------------------------------------------------------------

    private final String host;
    private final int port;
    

    private boolean initiated = false;
	private TimeUnit retryConnectionUnit = TimeUnit.SECONDS;

	public TimeUnit getRetryConnectionUnit() {
		return retryConnectionUnit;
	}

	public void setRetryConnectionUnit(TimeUnit retryConnectionUnit) {
		this.retryConnectionUnit = retryConnectionUnit;
	}
	private long retryConnectionPeriod = 30;
    public long getRetryConnectionPeriod() {
		return retryConnectionPeriod;
	}

	public void setRetryConnectionPeriod(long retryConnectionPeriod) {
		this.retryConnectionPeriod = retryConnectionPeriod;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OutboundEndpoint other = (OutboundEndpoint) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	/**
     * 
     * @param host
     * @param port
     */
    public OutboundEndpoint(String host, int port) {
        this(host, port, 1);
    }
    /**
     * 
     * @param host
     * @param port
     * @param maxConn
     */
    public OutboundEndpoint(String host, int port, int maxConn) {
        if ((port < 0) || (port > 65536)) {
            throw new IllegalArgumentException("Port must be in range 0-65536");
        }
        this.host = host;
        this.port = port;
        this.maxConnections = maxConn;
    }
    
    private static final int READY = 1;
    private static final int CLOSED = 3;
    private static final int OPEN = 0;
    private static final int CLOSING = 2;
    private TunnelOutboundHandler tunnelOutHandler;
    TunnelInboundHandler tunnelInHandler;
    /**
     * 
     * @author esutdal
     *
     */
    private class ChannelReadyListener implements ChannelFutureListener
    {

		@Override
		public void operationComplete(ChannelFuture future) {
			tunnelInHandler.endReconnectionAttempt(OutboundEndpoint.this.toString());
			if (future.isSuccess()) {
				synchronized (channelState) {
					//inboundChannel.read();
					channelState.compareAndSet(OPEN, READY);
					channelState.notifyAll();
					log.info("Successfully created tunnel to {} on LoadBalancer with id '{}'.",
							outboundChannel.remoteAddress(), OutboundEndpoint.this.hashCode());
					
					if (maxConnections > 1) {
						b.remoteAddress(future.channel().remoteAddress());
						b = b.clone(EventLoops.get());
						pooledChannels = new FixedChannelPool(b, OutboundEndpoint.this, maxConnections - 1);
					}
				}
			} else {
				synchronized (channelState) {
					channelState.compareAndSet(OPEN, OPEN);
					channelState.notifyAll();
					log.warn("Failed to create tunnel to {} on LoadBalancer with id '{}'.",OutboundEndpoint.this.toString(), OutboundEndpoint.this.hashCode());
				}
			}
		}
	
    }
    
    private Channel outboundChannel;
    
    private final AtomicInteger channelState = new AtomicInteger(OPEN);
	private int maxConnections = 3;
    public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}
	private final SynchronousQueue<Channel> pooledChannelSyncQ = new SynchronousQueue<>();
	/**
	 * Try acquire a pooled channel.
	 * @return
	 */
	private Channel acquirePooledChannel()
	{
		if(pooledChannels != null)
		{
			pooledChannels.acquire().addListener(new FutureListener<Channel>() {

				@Override
				public void operationComplete(Future<Channel> future) throws Exception  {
					if(future.isSuccess())
					{
						Channel c = future.getNow();
						boolean offered = false;
						try 
						{
							offered = pooledChannelSyncQ.offer(c, 1, TimeUnit.SECONDS);
						} 
						catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						finally
						{
							if(!offered)
								pooledChannels.release(c);
						}
					}
					else
					{
						log.warn("Failed to create pooled connection. Future cancelled?"+future.isCancelled()+" done?"+future.isDone(), future.cause());
						
					}

				}
			});
			try {
				return pooledChannelSyncQ.poll(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return null;
	}
	private Bootstrap b;
	/**
     * Associate a channel to this instance.
     * @param f
     * @param b 
     * @param inboundChannel 
     */
    synchronized void associate(ChannelFuture f, Bootstrap b)
    {
    	close0();
    	this.b = b;
    	f.addListener(new ChannelReadyListener());
    	outboundChannel = f.channel();
    	    	
    }
    /**
     * 
     * @param await
     * @param unit
     * @return
     */
    boolean awaitReady(long await, TimeUnit unit)
    {
    	synchronized (channelState) {
			if (!isReady()) {
				try {
					channelState.wait(unit.toMillis(await));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
    	
    	return isReady();
    }
    private FixedChannelPool pooledChannels;
    /**
     * If channel is ready.
     * @param timeout
     * @param unit
     * @return
     */
    boolean isReady()
    {
    	return outboundChannel != null 
    			&& outboundChannel.isActive() 
    			&& channelState.compareAndSet(READY, READY);
    }
    /**
     * Will return a pooled connection, or the unpooled.
     * @return
     */
    private PooledOrUnpooled getOutChannel()
    {
    	Channel ch = acquirePooledChannel();
    	//TODO: pooling is disabled. and thus so is 
    	//server side multithreading! this is because
    	//Netty pins a channel to a particular thread
    	//to avoid locking
    	ch = null;
    	return ch == null ? new PooledOrUnpooled(outboundChannel, false) : new PooledOrUnpooled(ch, true);
    }
    /**
     * Write to this endpoint.
     * @param clientCtx
     * @param msg
     */
    void write(final ChannelHandlerContext clientCtx, Object msg)
    {
    	log.debug("FORWARD TUNNEL WRITE => client 2 server");
    	final PooledOrUnpooled pooled = getOutChannel();
    	log.debug("Found target instance => "+pooled);
    	pooled.channel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				log.debug("Making reverse tunnel ready");
				if(pooled.pooled)
				{
					pooledChannels.release(pooled.channel);
				}
				//don't request read here
				getTunnelOutHandler().setClientChannel(clientCtx.channel());
			}
		});
    }
    
    
    // getters & setters ----------------------------------------------------------------------------------------------

    /* (non-Javadoc)
	 * @see com.reactiva.emulator.netty.gw.Endpoint#getHost()
	 */
    @Override
	public String getHost() {
        return host;
    }

    /* (non-Javadoc)
	 * @see com.reactiva.emulator.netty.gw.Endpoint#getPort()
	 */
    @Override
	public int getPort() {
        return port;
    }

    // low level overrides --------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return this.host + ':' + this.port;
    }
    private void close0()
	{
		if (outboundChannel != null) {
			synchronized (channelState) {
				outboundChannel.close().syncUninterruptibly();
				outboundChannel = null;
				channelState.compareAndSet(CLOSING, CLOSED);
				channelState.notifyAll();
			}
		}
		if(pooledChannels != null)
		{
			pooledChannels.close();
		}
	}
	@Override
	public void close() {
		channelState.getAndSet(CLOSING);
		close0();
	}

	/* (non-Javadoc)
	 * @see com.reactiva.emulator.netty.gw.Endpoint#getTunnelOutHandler()
	 */
	@Override
	public TunnelOutboundHandler getTunnelOutHandler() {
		return tunnelOutHandler;
	}

	/* (non-Javadoc)
	 * @see com.reactiva.emulator.netty.gw.Endpoint#setTunnelOutHandler(com.reactiva.emulator.netty.gw.TunnelOutboundHandler)
	 */
	@Override
	public void setTunnelOutHandler(TunnelOutboundHandler tunnelOutHandler) {
		this.tunnelOutHandler = tunnelOutHandler;
	}

	public boolean isInitiated() {
		return initiated;
	}

	public void setInitiated(boolean initiated) {
		this.initiated = initiated;
	}
	
	private final AtomicInteger activeConnections = new AtomicInteger();
	int incrConnection() {
		return activeConnections.incrementAndGet();
	}

	int decrConnection() {
		return activeConnections.decrementAndGet();
	}

	@Override
	public String identifier() {
		return toString();
	}

	private int weight = 0;
	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public int weight() {
		return weight;
	}

	@Override
	public int connections() {
		return activeConnections.get();
	}

	@Override
	public long ping() {
		return responseCount > 0 ? BigDecimal.valueOf(responseTimeAggr/responseCount).longValue() : Long.MAX_VALUE;
	}

	@Override
	public boolean responding() {
		return isReady();
	}

	private volatile long responseTimeAggr = 0;
	private volatile long responseCount = 0;
	/**
	 * 
	 * @param l
	 */
	synchronized void fireResponseTime(long l) {
		responseTimeAggr += l;
		responseCount++;
	}

	@Override
	public void channelReleased(Channel ch) throws Exception {
		log.info("channelReleased: "+ch.remoteAddress());
		
	}

	@Override
	public void channelAcquired(Channel ch) throws Exception {
		log.info("channelAcquired: "+ch.remoteAddress());
		
	}

	@Override
	public void channelCreated(Channel ch) throws Exception {
		log.info("channelCreated: "+ch.remoteAddress());
		
	}

}
