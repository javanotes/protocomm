package com.smsnow.adaptation.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRequestHandler implements RequestHandler {

	private static final Logger log = LoggerFactory.getLogger(AbstractRequestHandler.class);
	@Override
	public void init() {
		log.info("* Request handler "+name()+" init *");
		log.info("Mapping request "+requestMapping()+" to response "+responseMapping());
	}
	/**
	 * Override to provide a logical name
	 * @return
	 */
	public String name()
	{
		return getClass().getSimpleName().toUpperCase();
	}
	@Override
	public void destroy() {
		log.info(name() + " destroy");
		
	}


}
