package com.reactiva.emulator.netty.sh;

import org.springframework.beans.factory.FactoryBean;

public class RequestConvertorHandlerFactory implements FactoryBean<RequestConvertorHandler> {

	@Override
	public RequestConvertorHandler getObject() throws Exception {
		return new RequestConvertorHandler();
	}

	@Override
	public Class<?> getObjectType() {
		return RequestConvertorHandler.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}
