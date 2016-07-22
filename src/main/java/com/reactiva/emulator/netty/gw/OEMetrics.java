package com.reactiva.emulator.netty.gw;
/**
 * OutboundEndpoint metrics
 * @author esutdal
 *
 */
class OEMetrics
{
	public OEMetrics(OutboundEndpoint host) {
		super();
		this.host = host;
	}
	final OutboundEndpoint host;
	private volatile long markTime;
	public synchronized void onRequestSent()
	{
		markTime = System.currentTimeMillis();
	}
	public synchronized void onResponseReceived()
	{
		host.fireResponseTime(System.currentTimeMillis() - markTime);
	}
	public int incrementAndGet() {
		return host.incrConnection();
	}
	public int decrementAndGet() {
		return host.decrConnection();
	}
}