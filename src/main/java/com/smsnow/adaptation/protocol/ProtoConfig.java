package com.smsnow.adaptation.protocol;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.smsnow.adaptation.protocol.itoc.StreamedITOCCodec;

@Configuration
public class ProtoConfig {
	@Bean
	public StreamedITOCCodec streamCodec()
	{
		return new StreamedITOCCodec();
	}
}
