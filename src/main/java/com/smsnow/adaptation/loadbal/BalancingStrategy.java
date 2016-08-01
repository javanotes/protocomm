package com.smsnow.adaptation.loadbal;

import com.smsnow.adaptation.loadbal.Target.Algorithm;

public interface BalancingStrategy<T extends Target> {

	Algorithm strategy();
	T getNext();
}
