package com.smsnow.adaptation.server.gw;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.springframework.util.Assert;

import com.smsnow.adaptation.loadbal.BalancingStrategy;
import com.smsnow.adaptation.loadbal.Target.Algorithm;

class OutboundEndpoints implements BalancingStrategy<OutboundEndpoint>
{
	private final ConcurrentMap<String, ThreadLocal<OutboundEndpoint>> targets;
	private final Algorithm balancingAlgo;
	private BalancingStrategy<OutboundEndpoint> balancer;
	/**
	 * 
	 * @param targets
	 * @param tunnelInboundHandler 
	 */
	public OutboundEndpoints(List<OutboundEndpoint> targets, Algorithm balancingAlgo)
	{
		this.targets = new ConcurrentHashMap<>(targets.size());
		this.balancingAlgo = balancingAlgo;
		init(targets);
	}
	private ConcurrentMap<String, ThreadLocal<BalancingStrategy<OutboundEndpoint>>> balancers;
	private void initBalancer()
	{
		switch(balancingAlgo)
		{
		case FASTEST:
			break;
		case LEASTCONNECTION:
			break;
		case ROUNDROBIN:
			break;
		case WEIGHTED:
			break;
		default:
			break;
		
		}
	}
	private void init(List<OutboundEndpoint> targets)
	{
		for(final OutboundEndpoint oe : targets)
		{
			this.targets.put(oe.toString(), ThreadLocal.withInitial(new Supplier<OutboundEndpoint>() {

				@Override
				public OutboundEndpoint get() {
					return oe;
				}
			}));
		}
	}
	
	/**
	 * 
	 * @param oe
	 * @return
	 */
	OutboundEndpoint get(OutboundEndpoint oe)
	{
		Assert.isTrue(contains(oe));
		return targets.get(oe.toString()).get();
	}
	boolean contains(OutboundEndpoint oe)
	{
		return targets.containsKey(oe.toString());
	}
	void set(OutboundEndpoint oe)
	{
		targets.get(oe.toString()).set(oe);
	}
	public int size() {
		return targets.size();
	}
	
	@Override
	public Algorithm strategy() {
		return balancingAlgo;
	}
	@Override
	public OutboundEndpoint getNext() {
		// TODO Auto-generated method stub
		return null;
	}
}