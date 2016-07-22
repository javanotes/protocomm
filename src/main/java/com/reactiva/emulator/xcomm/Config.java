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
import com.reactiva.emulator.xcomm.sh.RequestConvertorHandlerFactory;
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
		return ioThread;
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

		public Map<String, Integer> getMaxpool() {
			return maxpool;
		}

		public void setMaxpool(Map<String, Integer> maxpool) {
			this.maxpool = maxpool;
		}

		private Map<String, String> target;
		private Map<String, Integer> maxpool;
	}
	private Thread monitorThread;
	@PostConstruct
	public void init() throws InterruptedException
	{
		server().startServer();
		monitorThread = new Thread(server(), "TCPMon");
		monitorThread.setDaemon(true);
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
		TCPConnector s = new TCPConnector(port, ioThread, execThread, proxyMode);
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
