package com.smsnow.adaptation.server;

import java.io.Serializable;
/**
 * A request handler based on the {@link #requestMapping()} type. Messages are 
 * mapped to handlers. If we do not have any client specific information present in the
 * message, then it is advisable to have a single handler type for each message request.
 * However, it is advisable to get a request dispatcher that can route to multiple handlers
 * based on the message type.
 * @author esutdal
 *
 */
public interface RequestHandler {

	/**
	 * 
	 * @return request class type
	 */
	<X extends Serializable> Class<X> requestMapping();
	/**
	 * 
	 * @return response class type
	 */
	<Y extends Serializable> Class<Y> responseMapping();
	/**
	 * 
	 */
	void init();
	/**
	 * 
	 */
	void destroy();
	/**
	 * Process the request.
	 * @param request
	 * @return response
	 * @throws Exception
	 */
	Serializable process(Serializable request) throws Exception;
}
