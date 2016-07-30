package com.reactiva.emulator.xcomm.sh;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smsnow.protocol.IType;

public class RequestDispatcher implements RequestHandler {

	private static final Logger log = LoggerFactory.getLogger(RequestDispatcher.class);
	
	private Map<Short, RequestHandler> allHandlers = new HashMap<>();
	private Map<Short, String> handlers = new HashMap<>();
	@Override
	public IType service(IType request) throws Exception {
		if(!allHandlers.containsKey(request.code()))
		{
			if(handlers.containsKey(request.code()))
			{
				synchronized (allHandlers) {
					if(!allHandlers.containsKey(request.code()))
					{
						try {
							RequestHandler rh = (RequestHandler) Class.forName(handlers.get(request.code())).newInstance();
							rh.init();
							allHandlers.put(request.code(), rh);
						} catch (Exception e) {
							throw e;
						}
					}
				}
			}
			else
			{
				throw new IllegalArgumentException("No request handler defined for message type- "+request.code());
			}
			
		}
		return allHandlers.get(request.code()).service(request);
	}
	@PostConstruct
	@Override
	public void init() 
	{
		//probably load on startup classes here
	}
	@PreDestroy
	@Override
	public void destroy() {
		for(Entry<Short, RequestHandler> s : allHandlers.entrySet())
		{
			try {
				s.getValue().destroy();
			} catch (Exception e) {
				log.warn(e.getMessage());
				log.debug(e.getMessage(), e);
			}
		}
		
	}
	public Map<Short, String> getHandlers() {
		return handlers;
	}
	public void setHandlers(Map<Short, String> handlers) {
		this.handlers.putAll(handlers);
	}

}
