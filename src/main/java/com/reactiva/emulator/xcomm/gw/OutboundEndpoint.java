package com.reactiva.emulator.xcomm.gw;

import java.io.Closeable;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reactiva.emulator.xcomm.gw.bal.Target;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.pool.ChannelPoolHandler;
/**
 * 
 * @author esutdal
 *
 */
public class OutboundEndpoint implements Closeable, Target, ChannelPoolHandler, Endpoint{

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
        if ((port < 0) || (port > 65536)) {
            throw new IllegalArgumentException("Port must be in range 0-65536");
        }
        this.host = host;
        this.port = port;
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
    /**
     * Associate a channel to this instance.
     * @param f
     * @param inboundChannel 
     */
    synchronized void associate(ChannelFuture f)
    {
    	close0();
    	f.addListener(new ChannelReadyListener());
    	this.outboundChannel = f.channel();
    	    	
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
     * Override this method to use a connection pool.
     * @return
     */
    private Channel getOutChannel()
    {
    	return outboundChannel;
    }
    /**
     * Write to this endpoint.
     * @param ctx
     * @param msg
     */
    void write(final ChannelHandlerContext ctx, Object msg)
    {
    	log.debug("FORWARD TUNNEL WRITE => client 2 server");
    	
		getOutChannel().writeAndFlush(msg).addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				log.debug("Making reverse tunnel ready");
				//don't request read here
				getTunnelOutHandler().setClientChannel(ctx.channel());
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void channelAcquired(Channel ch) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void channelCreated(Channel ch) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
