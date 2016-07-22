package com.reactiva.emulator.netty.gw.bal;

import java.util.List;

import com.reactiva.emulator.netty.gw.bal.Target.Algorithm;

public class Balancer {

	private Balancer(){}
	/**
	 * Return a new balancer implementing the give algorithm.
	 * @param <T>
	 * @param targets
	 * @param algo
	 * @return
	 */
	public static <T extends Target> BalancingStrategy<T> getBalancer(List<T> targets, Algorithm algo)
	{
		switch (algo) {
		case FASTEST:
			return new FastestBalancingStrategy<>(targets);
		case LEASTCONNECTION:
			return new LeastConnectionBalncingStrategy<>(targets);
		case ROUNDROBIN:
			return new RoundRobinBalancingStrategy<>(targets);
		case WEIGHTED:
			return new WeightedBalancingStrategy<>(targets);
		default:
			throw new IllegalArgumentException();
		
		}
	}
}
