package com.reactiva.emulator.xcomm.sh;

import org.springframework.beans.factory.FactoryBean;

public class RequestConvertorHandlerFactory implements FactoryBean<RequestConvertorHandler> {
	public RequestConvertorHandlerFactory(ITOCCodecHandler ch) {
		this.ch = ch;
	}
	private ITOCCodecHandler ch;
	public ITOCCodecHandler getCh() {
		return ch;
	}

	public void setCh(ITOCCodecHandler ch) {
		this.ch = ch;
	}

	@Override
	public RequestConvertorHandler getObject() throws Exception {
		return new RequestConvertorHandler(ch);
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
