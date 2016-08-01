package com.smsnow.adaptation.server;

import java.io.Serializable;

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
