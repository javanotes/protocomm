package com.smsnow.adaptation.server.gw;

import io.netty.channel.Channel;

class PooledOrUnpooled
{
	@Override
	public String toString() {
		return "PooledOrUnpooled [channel=" + channel.remoteAddress() + ", pooled=" + pooled + "]";
	}
	public PooledOrUnpooled(Channel ch, boolean pooled) {
		super();
		this.channel = ch;
		this.pooled = pooled;
	}
	final Channel channel;
	final boolean pooled;
}