package com.reactiva.emulator.xcomm.gw;

public interface Endpoint {

	String getHost();

	int getPort();

	TunnelOutboundHandler getTunnelOutHandler();

	void setTunnelOutHandler(TunnelOutboundHandler tunnelOutHandler);

}