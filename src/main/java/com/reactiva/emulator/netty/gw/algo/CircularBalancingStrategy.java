package com.reactiva.emulator.netty.gw.algo;

import java.util.LinkedList;
import java.util.List;

public class CircularBalancingStrategy<T extends Target> implements BalancingStrategy {

	private LinkedList<T> targets;
	CircularBalancingStrategy() {
		
	}
	public CircularBalancingStrategy(List<T> targets) {
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

}
