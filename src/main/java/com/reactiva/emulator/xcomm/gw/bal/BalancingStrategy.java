package com.reactiva.emulator.xcomm.gw.bal;

import com.reactiva.emulator.xcomm.gw.bal.Target.Algorithm;

public interface BalancingStrategy<T extends Target> {

	Algorithm strategy();
	T getNext();
}
