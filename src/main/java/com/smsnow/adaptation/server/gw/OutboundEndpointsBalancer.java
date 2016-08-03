package com.smsnow.adaptation.server.gw;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.springframework.util.Assert;

import com.smsnow.adaptation.loadbal.Balancer;
import com.smsnow.adaptation.loadbal.BalancingStrategy;
import com.smsnow.adaptation.loadbal.Target.Algorithm;
/**
 * A thread local balancer for outbound endpoints.
 * @author esutdal
 *
 */
class OutboundEndpointsBalancer implements BalancingStrategy<OutboundEndpoint>
{
	private final ConcurrentMap<String, ThreadLocal<OutboundEndpoint>> targets;
	private final BalancingStrategy<OutboundEndpoint> balancer;
	private Algorithm balancingAlgo;
	/**
	 * 
	 * @param targets
	 * @param tunnelInboundHandler 
	 */
	public OutboundEndpointsBalancer(List<OutboundEndpoint> targets, Algorithm balancingAlgo)
	{
		this.targets = new ConcurrentHashMap<>(targets.size());
		balancer = Balancer.getBalancer(targets, balancingAlgo);
		init(targets);
		this.balancingAlgo = balancingAlgo;
	}
	
	private void init(List<OutboundEndpoint> targets)
	{
		for(final OutboundEndpoint oe : targets)
		{
			this.targets.put(oe.toString(), ThreadLocal.withInitial(new Supplier<OutboundEndpoint>() {

				@Override
				public OutboundEndpoint get() {
					return new OutboundEndpoint(oe.getHost(), oe.getPort());
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
		OutboundEndpoint target = balancer.getNext();
    	Assert.notNull(target);
    	return get(target);
	}
}