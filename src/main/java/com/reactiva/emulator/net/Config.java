package com.reactiva.emulator.net;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

//@Configuration
public class Config {

	@Value("${server.gw: false}")
	boolean proxyMode;
	@Value("${server.port}")
	int port;
	@Value("${server.io-threads: 1}")
	int ioThread;
	
	@Value("${server.exec-threads: 4}")
	int execThread;
	
	@Value("${server.proto.len.max: 1000}")
	int protoLenMax;
	@Value("${server.proto.len.offset: 0}")
	int protoLenOffset;
	@Value("${server.proto.len.bytes: 4}")
	int protoLenBytes;
	@Value("${server.proto.closeOnFlush: false}")
	boolean closeOnFlush;
	@PostConstruct
	void init() throws IOException
	{
		server().startServer();
	}
	@PreDestroy
	void destroy() throws IOException
	{
		server().stopServer();
	}
	@Bean
	ProtocolHandlerFactory proto()
	{
		return new ProtocolHandlerFactory();
	}
	@Bean
	@DependsOn({"proto"})
	ServerSocketListener server() throws IOException
	{
		return new ServerSocketListener(port, execThread, 1, ioThread);
	}
}
