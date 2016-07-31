package com.reactiva.emulator.xcomm.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reactiva.emulator.xcomm.sh.RequestHandler;
import com.smsnow.protocol.FixedLengthType;

public class ITOCLoginHandler implements RequestHandler {

	private static final Logger log = LoggerFactory.getLogger(ITOCLoginHandler.class);
	@Override
	public void init() {
		log.info("############ handler init ##########");
	}

	@Override
	public void destroy() {
		log.info("############ handler destroy ##########");
	}

	@Override
	public FixedLengthType service(FixedLengthType request) throws Exception {
		ITOCLogin req = (ITOCLogin) request;
		log.info("Got request:: "+req);
		req.setAction("B");
		return req;
	}



}
