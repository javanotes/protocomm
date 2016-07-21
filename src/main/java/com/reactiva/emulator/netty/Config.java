package com.reactiva.emulator.netty;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.reactiva.emulator.netty.sh.BasicChannelHandler;
import com.reactiva.emulator.netty.sh.RequestConvertorHandlerFactory;
import com.reactiva.emulator.netty.sh.RequestProcessorHandler;
import com.reactiva.emulator.netty.sh.ResponseConvertorHandler;
import com.reactiva.emulator.netty.sh.TerminalHandler;

@Configuration
public class Config {

	boolean isProxyMode() {
		return proxyMode;
	}
	int getPort() {
		return port;
	}
	int getMaxThread() {
		return maxThread;
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
	@Value("${server.io-threads: 2}")
	int maxThread;
	
	@Value("${server.proto.len.max: 1000}")
	int protoLenMax;
	@Value("${server.proto.len.offset: 0}")
	int protoLenOffset;
	@Value("${server.proto.len.bytes: 4}")
	int protoLenBytes;
	@Value("${server.proto.closeOnFlush: false}")
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
	public void init() throws InterruptedException
	{
		server().startServer();
		monitorThread = new Thread(server(), "TCPMon");
		monitorThread.start();
	}
	@PreDestroy
	public void destroy()
	{
		server().stopServer();
		server().stopMonitor();
		monitorThread.interrupt();
	}
	@Bean
	@DependsOn({"decoder", "processor", "encoder", "targets"})
	public TCPConnector server()
	{
		TCPConnector s = new TCPConnector(port, maxThread, proxyMode);
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
		
		return s;
	}
	@Bean
	RequestConvertorHandlerFactory decoder()
	{
		return new RequestConvertorHandlerFactory();
	}
	@Bean
	RequestProcessorHandler processor()
	{
		return new RequestProcessorHandler();
	}
	@Bean
	ResponseConvertorHandler encoder()
	{
		return new ResponseConvertorHandler();
	}
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