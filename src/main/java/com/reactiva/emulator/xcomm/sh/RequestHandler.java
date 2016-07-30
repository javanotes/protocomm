package com.reactiva.emulator.xcomm.sh;

import com.smsnow.protocol.IType;

public interface RequestHandler {

	void init();
	IType service(IType request) throws Exception;
	void destroy();
}
