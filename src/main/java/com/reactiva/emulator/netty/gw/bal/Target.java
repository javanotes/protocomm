package com.reactiva.emulator.netty.gw.bal;

import java.io.Closeable;

/**
 * This interface needs to be implemented to determine the target node and its metrics. 
 * None of the methods should throw any checked exception.
 * @author esutdal
 *
 */
public interface Target extends Closeable{
	
	public enum Algorithm{ROUNDROBIN, WEIGHTED, LEASTCONNECTION, FASTEST}
	/**
	 * 
	 * @return an identifier for a particular node
	 */
	public String identifier();
	
	/**
	 * 
	 * @return weight index for this node
	 */
	public int weight();
	/**
	 * 
	 * @return no of active requests already being served by this node
	 */
	public int connections();
	/**
	 * 
	 * @return response time from the server
	 */
	public long ping();
	/**
	 * 
	 * @return true if the node is accessible
	 */
	public boolean responding();
	
}
