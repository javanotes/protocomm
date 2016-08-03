package com.smsnow.adaptation.server.rh;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smsnow.adaptation.server.AbstractRequestHandler;
import com.smsnow.perf.ITOCLogin;

public class ITOCLoginHandler extends AbstractRequestHandler {

	private static final Logger log = LoggerFactory.getLogger(ITOCLoginHandler.class);
	@SuppressWarnings("unchecked")
	@Override
	public Class<ITOCLogin> requestMapping() {
		return ITOCLogin.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<ITOCLogin> responseMapping() {
		return ITOCLogin.class;
	}

	@Override
	public Serializable process(Serializable request) throws Exception {
		ITOCLogin dto = (ITOCLogin) request;
		if (log.isDebugEnabled()) {
			log.debug("### Got new request: " + request);
			log.debug("Capitalizing login id");
		}
		dto.setUserLogonID(dto.getUserLogonID().toUpperCase());
		return dto;
	}

}
