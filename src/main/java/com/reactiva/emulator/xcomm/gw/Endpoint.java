package com.reactiva.emulator.xcomm.gw;

public interface Endpoint {

	String getHost();

	int getPort();
	
	boolean isUnInitiated();

	TunnelOutboundHandler getTunnelOutHandler();

	void setTunnelOutHandler(TunnelOutboundHandler tunnelOutHandler);

}