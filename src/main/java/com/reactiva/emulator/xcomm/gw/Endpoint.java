package com.reactiva.emulator.xcomm.gw;

public interface Endpoint {

	String getHost();

	int getPort();
	
	boolean isUnInitiated();
	/**
	 * This is  synchronized access.
	 * @param tunnelOutHandler
	 */
	void setTunnelOutHandler(TunnelOutboundHandler tunnelOutHandler);

}