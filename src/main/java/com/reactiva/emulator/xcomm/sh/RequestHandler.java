package com.reactiva.emulator.xcomm.sh;

public interface RequestHandler {

	void init();
	Object service(Object request) throws Exception;
	void destroy();
}
