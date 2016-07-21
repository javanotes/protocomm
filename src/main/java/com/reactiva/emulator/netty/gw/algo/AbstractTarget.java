package com.reactiva.emulator.netty.gw.algo;

import java.util.UUID;

public abstract class AbstractTarget implements Target {

	
	@Override
	public void restart() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String identifier() {
		return UUID.randomUUID().toString();
	}

	@Override
	public int weight() {
		return 1;
	}

	@Override
	public int connections() {
		return 0;
	}

	@Override
	public long ping() {
		return 0;
	}

	@Override
	public boolean responding() {
		return false;
	}

}
