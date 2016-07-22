package com.reactiva.emulator.netty.gw.bal;

import com.reactiva.emulator.netty.gw.bal.Target.Algorithm;

public interface BalancingStrategy<T extends Target> {

	Algorithm strategy();
	T getNext();
}
