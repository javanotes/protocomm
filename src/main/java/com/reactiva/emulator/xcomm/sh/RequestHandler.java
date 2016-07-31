package com.reactiva.emulator.xcomm.sh;

import com.smsnow.protocol.FixedLengthType;

public interface RequestHandler {

	void init();
	FixedLengthType service(FixedLengthType request) throws Exception;
	void destroy();
}
