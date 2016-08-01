package com.smsnow.adaptation.loadbal;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.smsnow.adaptation.loadbal.Target.Algorithm;

class WeightedBalancingStrategy<T extends Target> extends RoundRobinBalancingStrategy<T> {

	@Override
	public Algorithm strategy() {
		return Algorithm.WEIGHTED;
	}
	private final LinkedList<T> targets;
	public WeightedBalancingStrategy(List<T> targets) {
		this.targets = new LinkedList<>();
		Collections.sort(targets, new Comparator<T>() {

			@Override
			public int compare(T arg0, T arg1) {
				return Integer.compare(arg0.weight(), arg1.weight());
			}
		});
		for(T t : targets)
		{
			for(int i=0; i<t.weight(); i++)
			{
				this.targets.add(t);
			}
		}
	}
	
}
