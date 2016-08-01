package com.smsnow.adaptation.server;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.smsnow.adaptation.server.pipe.RequestProcessorHandler;
import com.smsnow.adaptation.server.pipe.ResponseConvertorHandler;
import com.smsnow.adaptation.server.pipe.TerminalHandler;
import com.smsnow.protocol.ITOCCodec;

@Configuration
public class Config {

	boolean isProxyMode() {
		return proxyMode;
	}
	int getPort() {
		return port;
	}
	int getMaxThread() {
		return ioThreadCount;
	}
	int getProtoLenMax() {
		return protoLenMax;
	}
	int getProtoLenOffset() {
		return protoLenOffset;
	}
	int getProtoLenBytes() {
		return protoLenBytes;
	}
	@Value("${server.gw: false}")
	boolean proxyMode;
	@Value("${server.port}")
	int port;
	@Value("${server.io-threads: 1}")
	int ioThreadCount;
	
	@Value("${server.exec-threads: 4}")
	int execThreadCount;
	@Value("${server.monitor.enable: false}")
	boolean monitor;
	@Value("${server.handler-class}")
	String requestHandler;
	
	public boolean isMonitorEnabled() {
		return monitor;
	}
	@Value("${server.proto.len.max: 1000}")
	int protoLenMax;
	@Value("${server.proto.len.offset: 0}")
	int protoLenOffset;
	@Value("${server.proto.len.bytes: 4}")
	int protoLenBytes;
	@Value("${server.proto.close-on-flush: false}")
	boolean closeOnFlush;
	@Bean
	@ConfigurationProperties(prefix = "server.gw")
	public HostAndPort targets()
	{
		return new HostAndPort();
	}
			
	public static class HostAndPort
	{
		@Override
		public String toString() {
			return "HostAndPort [target=" + target + "]";
		}

		public Map<String, String> getTarget() {
			return target;
		}

		public void setTarget(Map<String, String> target) {
			this.target = target;
		}

		private Map<String, String> target;
	}
	private Thread monitorThread;
	@PostConstruct
	public void init() throws Exception
	{
		server().startServer();
		if (isMonitorEnabled()) {
			monitorThread = new Thread(server(), "TCPMon");
			monitorThread.setDaemon(true);
			monitorThread.start();
		}
	}
	@PreDestroy
	public void destroy() throws Exception
	{
		server().stopServer();
		server().stopMonitor();
		if (monitorThread != null) {
			monitorThread.interrupt();
		}
		
		handler().destroy();
	}
	@Bean
	@DependsOn({"encoder", "processor", "targets"})
	public TCPConnector server() throws Exception
	{
		TCPConnector s = new TCPConnector(port, ioThreadCount, execThreadCount, proxyMode);
		s.setConfig(this);
		//in server mode, initialize request handlers.
		if (!proxyMode) {
			s.setRequestHandler(handler());
			s.setCodecHandler(codec());
			s.encoder = encoder();
			s.processor = processor();
			s.terminal = terminal();
		}
		else
		{
			//proxy mode
			System.err.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.err.println("|  ** WARN: Proxy implementation is unstable **  |");
			System.err.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
		}
		
		return s;
	}
	
	
	@Bean
	@DependsOn("handler")
	RequestProcessorHandler processor() throws Exception
	{
		return new RequestProcessorHandler(handler());
	}
	@Bean
	@DependsOn("codec")
	public RequestHandler handler() throws Exception {
		RequestHandler rh = (RequestHandler) Class.forName(requestHandler).newInstance();
		codec().sizeof(rh.requestMapping());
		codec().sizeof(rh.responseMapping());
		rh.init();
		return rh;
	}
	@Bean
	@DependsOn({"codec", "handler"})
	ResponseConvertorHandler encoder() throws Exception
	{
		return new ResponseConvertorHandler(codec(), handler());
	}
	@Bean
	ITOCCodec codec()
	{
		return new ITOCCodec();
	}
	
	@Bean
	TerminalHandler terminal()
	{
		TerminalHandler t = new TerminalHandler();
		t.setCloseOnFlush(closeOnFlush);
		return t;
	}
	
}
