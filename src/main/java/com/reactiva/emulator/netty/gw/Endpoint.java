package com.reactiva.emulator.netty.gw;

public interface Endpoint {

	String getHost();

	int getPort();

	TunnelOutboundHandler getTunnelOutHandler();

	void setTunnelOutHandler(TunnelOutboundHandler tunnelOutHandler);

}