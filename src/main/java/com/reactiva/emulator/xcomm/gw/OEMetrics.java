package com.reactiva.emulator.xcomm.gw;
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
	private volatile long lastRequestSentTS = 0, lastResponseTime = 0;
	@Override
	public synchronized String toString()
	{
		StringBuilder s = new StringBuilder();
		s.append("<OEMetrics> ");
		s.append("Host=").append(host).append("; ");
		s.append("Connections="+host.connections()+"; Last Response Time="+lastResponseTime);
		s.append(" </OEMetrics>");
		return s.toString();
	}
	/**
	 * Mark a request time.
	 */
	public synchronized void onRequestSent()
	{
		lastRequestSentTS = System.currentTimeMillis();
	}
	/**
	 * Mark a response time.
	 */
	public synchronized void onResponseReceived()
	{
		lastResponseTime = System.currentTimeMillis() - lastRequestSentTS;
		host.fireResponseTime(lastResponseTime);
	}
	public int incrementAndGet() {
		return host.incrConnection();
	}
	public int decrementAndGet() {
		return host.decrConnection();
	}
}