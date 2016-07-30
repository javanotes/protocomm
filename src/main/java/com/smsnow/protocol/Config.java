package com.smsnow.protocol;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

	@Bean
	public DynamicCodec codec()
	{
		return new ITOCCodec();
	}
}
