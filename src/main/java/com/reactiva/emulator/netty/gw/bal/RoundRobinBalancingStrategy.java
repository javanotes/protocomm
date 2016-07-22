package com.reactiva.emulator.netty.gw.bal;

import java.util.LinkedList;
import java.util.List;

import com.reactiva.emulator.netty.gw.bal.Target.Algorithm;

class RoundRobinBalancingStrategy<T extends Target> implements BalancingStrategy<T> {

	private LinkedList<T> targets;
	RoundRobinBalancingStrategy() {
		
	}
	public RoundRobinBalancingStrategy(List<T> targets) {
		this.targets = new LinkedList<>(targets);
	}
	@Override
	public T getNext() {
		T t = targets.pollFirst();
		try
		{
			return t;
		}
		finally
		{
			targets.addLast(t);
		}
	}
	@Override
	public Algorithm strategy() {
		return Algorithm.ROUNDROBIN;
	}

}
