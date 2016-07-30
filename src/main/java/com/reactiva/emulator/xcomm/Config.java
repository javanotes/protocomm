package com.reactiva.emulator.xcomm;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.reactiva.emulator.xcomm.sh.BasicChannelHandler;
import com.reactiva.emulator.xcomm.sh.ITOCCodecHandler;
import com.reactiva.emulator.xcomm.sh.RequestConvertorHandlerFactory;
import com.reactiva.emulator.xcomm.sh.RequestDispatcher;
import com.reactiva.emulator.xcomm.sh.RequestProcessorHandler;
import com.reactiva.emulator.xcomm.sh.ResponseConvertorHandler;
import com.reactiva.emulator.xcomm.sh.TerminalHandler;

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
	@Bean
	@ConfigurationProperties(prefix = "server.codecs")
	public ITOCCodecHandler codecs()
	{
		return new ITOCCodecHandler();
	}
	@Bean
	@ConfigurationProperties(prefix = "server.handlers")
	public RequestDispatcher handlers()
	{
		return new RequestDispatcher();
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
	public void init() throws InterruptedException
	{
		server().startServer();
		if (isMonitorEnabled()) {
			monitorThread = new Thread(server(), "TCPMon");
			monitorThread.setDaemon(true);
			monitorThread.start();
		}
	}
	@PreDestroy
	public void destroy()
	{
		server().stopServer();
		server().stopMonitor();
		if (monitorThread != null) {
			monitorThread.interrupt();
		}
	}
	@Bean
	@DependsOn({"decoder", "processor", "encoder", "targets"})
	public TCPConnector server()
	{
		TCPConnector s = new TCPConnector(port, ioThreadCount, execThreadCount, proxyMode);
		s.setConfig(this);
		//in server mode, initialize request handlers.
		if (!proxyMode) {
			
			//this is for backward compatibility
			s.addHandler(bch());
			
			s.decoder = decoder();
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
	
	/* -------------------------------- */
	/*
	 * These are the extension points for the server implementation.
	 * Create new classes extending the base classes, and override the 
	 * protected methods.
	 */
	@Bean
	@DependsOn("codecs")
	RequestConvertorHandlerFactory decoder()
	{
		return new RequestConvertorHandlerFactory(codecs());
	}
	@Bean
	@DependsOn("handlers")
	RequestProcessorHandler processor()
	{
		return new RequestProcessorHandler(handlers());
	}
	@Bean
	@DependsOn("codecs")
	ResponseConvertorHandler encoder()
	{
		return new ResponseConvertorHandler(codecs());
	}
	/* -------------------------------- */
	
	@Bean
	TerminalHandler terminal()
	{
		TerminalHandler t = new TerminalHandler();
		t.setCloseOnFlush(closeOnFlush);
		return t;
	}
	@Bean
	BasicChannelHandler bch()
	{
		return new BasicChannelHandler();
	}
}
