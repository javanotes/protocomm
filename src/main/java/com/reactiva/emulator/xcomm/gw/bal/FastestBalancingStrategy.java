package com.reactiva.emulator.xcomm.gw.bal;

import java.util.ArrayList;
import java.util.List;

import com.reactiva.emulator.xcomm.gw.bal.Target.Algorithm;

class FastestBalancingStrategy<T extends Target> implements BalancingStrategy<T> {

	private ArrayList<T> targets;

	public FastestBalancingStrategy(List<T> targets) {
		this.targets = new ArrayList<>(targets);
	}
	@Override
	public Algorithm strategy() {
		return Algorithm.FASTEST;
	}

	@Override
	public T getNext() {
		long min = Integer.MAX_VALUE;
		T t = null;
		for(int i=0; i<targets.size(); i++)
		{
			if(targets.get(i).ping() < min)
			{
				t = targets.get(i);
				min = t.ping();
			}
		}
		return t;
	}

}
