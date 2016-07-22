package com.reactiva.emulator.xcomm.gw;

import java.util.function.Supplier;

import io.netty.channel.nio.NioEventLoopGroup;

class EventLoops
{
	private static ThreadLocal<NioEventLoopGroup> eventLoops = ThreadLocal.withInitial(new Supplier<NioEventLoopGroup>() {

		@Override
		public NioEventLoopGroup get() {
			return new NioEventLoopGroup();
		}
	});
	
	static NioEventLoopGroup get()
	{
		return eventLoops.get();
	}
}