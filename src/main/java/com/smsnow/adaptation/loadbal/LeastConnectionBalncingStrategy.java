package com.smsnow.adaptation.loadbal;

import java.util.ArrayList;
import java.util.List;

import com.smsnow.adaptation.loadbal.Target.Algorithm;

class LeastConnectionBalncingStrategy<T extends Target> implements BalancingStrategy<T> {

	private ArrayList<T> targets;

	public LeastConnectionBalncingStrategy(List<T> targets) {
		this.targets = new ArrayList<>(targets);
	}
	@Override
	public Algorithm strategy() {
		return Algorithm.LEASTCONNECTION;
	}

	@Override
	public T getNext() {
		int min = Integer.MAX_VALUE;
		T t = null;
		for(int i=0; i<targets.size(); i++)
		{
			if(targets.get(i).connections() < min)
			{
				t = targets.get(i);
				min = t.connections();
			}
		}
		return t;
	}

}
