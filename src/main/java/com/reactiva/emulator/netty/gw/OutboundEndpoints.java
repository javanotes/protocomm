package com.reactiva.emulator.netty.gw;

import java.util.Collections;
import java.util.List;

class OutboundEndpoints extends OutboundEndpoint{

	public OutboundEndpoints(String host, int port) {
		super(host, port);
	}

	private List<OutboundEndpoint> hosts = null;
	
	public List<OutboundEndpoint> getHosts() {
		return hosts;
	}

	void setHosts(List<OutboundEndpoint> hosts)
	{
		if(this.hosts == null)
		{
			synchronized(this)
			{
				if(this.hosts == null && hosts != null)
				{
					this.hosts = Collections.synchronizedList(hosts);
				}
			}
		}
	}
}
