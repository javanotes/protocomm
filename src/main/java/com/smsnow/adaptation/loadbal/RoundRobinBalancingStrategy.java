package com.smsnow.adaptation.loadbal;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.util.Assert;

import com.smsnow.adaptation.loadbal.Target.Algorithm;
import com.smsnow.adaptation.utils.Synchronizer;

class RoundRobinBalancingStrategy<T extends Target> implements BalancingStrategy<T> {

	private final LinkedList<T> targets;
	RoundRobinBalancingStrategy() {
		this(Collections.emptyList());
	}
	public RoundRobinBalancingStrategy(List<T> targets) {
		this.targets = new LinkedList<>();
		this.targets.addAll(targets);
		Assert.notEmpty(this.targets, "No Target provided");
	}
	private final Synchronizer sync = new Synchronizer();
	@Override
	public T getNext() {
		T t = null;
		sync.begin();
		try {
			t = targets.pollFirst();
			return t;
		} finally {
			targets.addLast(t);
			sync.end();
		}
	}
	@Override
	public Algorithm strategy() {
		return Algorithm.ROUNDROBIN;
	}

}
