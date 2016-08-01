package com.reactiva.protoserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.smsnow.adaptation.server.dto.ITOCLogin;
import com.smsnow.protocol.CodecException;
import com.smsnow.protocol.ITOCCodec;

public class Client {

	static void sendRequest(String msg) throws UnknownHostException, IOException
	{
		Socket s = new Socket("localhost", PORT);
		try {
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			DataInputStream in = new DataInputStream(s.getInputStream());
			byte[] req = msg.getBytes(StandardCharsets.UTF_8);
			out.writeInt(req.length+4);
			out.write(req);
			out.flush();
			System.out.println("Client => "+msg +"\t" + in.readUTF());
		} finally {
			s.close();
		}
	}
	
	
	static void sendRequests(final String msg) throws UnknownHostException, IOException
	{
		Socket s = new Socket("localhost", PORT);
		try {
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			DataInputStream in = new DataInputStream(s.getInputStream());
			for (int i = 0; i < CYCLE; i++) {
				
				byte[] req = (msg/* + "Request# "+i*/).getBytes(StandardCharsets.UTF_8);
				out.writeInt(req.length + 4);
				out.write(req);
				out.flush();
				in.readUTF();
			}
		} finally {
			s.close();
		}
	}
	
		
	static final String SHORT_MSG = "HELLO SOMOS ";
	static final String MEDIUM_MSG = "This is a medium text. Contains numb3r5 and, % character$. ";
	static final String LONG_MSG = "SAC    7326995108BRSAC*****    																			"
			+ "CSAC     0000UUUUYU480YYBUVUYUUUUUBBBYBBBDBYUUUUUYU001WILLIAM.MCCLEW@ERICSSON.COM  "
			+ "                                                          BYY UY UBBBBBBBBBUBBBBUUUUBBBYUBRSAUUBBBBBBBBVBBBYBBBBUYUUUUU"
			+"..ZM3TN06A         SWITCH  DW           ....                                                              ..                                                                                ..                 ..      ..BRSACWGM1465304324810       1465319626334      ......                    "
			+ "GRSP-NSR:,2016-06-07,13-10-15-CST:::COMPLD,00::ID=BRSACWGM,RO=BRSAC:CNT=02:NUM=\"8888347500        \":NUM=\"8888347501  ";
	
	
	private static void runPerf()
	{

		final AtomicLong sessStats = new AtomicLong();
		
		ExecutorService ex = Executors.newCachedThreadPool();
		try {
			System.out.println("Message bytes len => "+codec.sizeof(login.getClass()));
		} catch (CodecException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Client.main() #### START");
		long start = System.currentTimeMillis();
		for (int i = 0; i < CONCURRENCY; i++) {
			ex.submit(new Runnable() {

				@Override
				public void run() {
					
					for (int i = 0; i < ITERATION; i++) {
						try 
						{
							//sendRequest("HELLO SOMOS " + i);
							long start = System.currentTimeMillis();
							//sendRequests(msg);
							sendRequestCodec(CYCLE);
							long end = System.currentTimeMillis();
							sessStats.addAndGet(end-start);
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
				}
			});
		}
		ex.shutdown();
		try {
			ex.awaitTermination(180, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			
		}
		long stop = System.currentTimeMillis();
		System.out.println("Total Requests completed:: "+(ITERATION*CONCURRENCY*CYCLE));
		System.out.println("Total sessions:: "+(CONCURRENCY*ITERATION));
		System.out.println("Requests per session:: "+(CYCLE));
		System.out.println("Total Time:: "+timeString(stop-start));
		System.out.println("Avg Time per session:: "+timeString(sessStats.get()/(CONCURRENCY*ITERATION)));
		System.out.println("Avg Time per request:: "+timeString(sessStats.get()/(CONCURRENCY*ITERATION*CYCLE)));
	
	}
	
	static int ITERATION = 1, PORT = 8081, CONCURRENCY = 1000, CYCLE = 100;
	
	public static void main(String[] args) throws Exception {
		//simpleTest();
		//simpleConcurrentTest();
		runPerf();
		
		//sendRequestCodec(10);
		
	}
	static final ITOCCodec codec = new ITOCCodec();
	static final ITOCLogin login = new ITOCLogin("sutanu81");
	
	private static void sendRequestCodec(int n) throws UnknownHostException, IOException, CodecException
	{
		Socket s = new Socket("localhost", PORT);
		try 
		{
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			DataInputStream in = new DataInputStream(s.getInputStream());
				
			
			for (int i = 0; i < n; i++) {
				codec.encode(login, out);
				out.flush();
				//System.out.println("Sent request..");
				ITOCLogin resp = codec.decode(ITOCLogin.class, in);
				//System.out.println("Response => " + resp);
			}
			
		} finally {
			s.close();
		}
	}
	private static void simpleTest()
	{
		for(int i=0; i<ITERATION; i++)
		{
			try {
				sendRequest(SHORT_MSG + i);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private static void simpleConcurrentTest()
	{
		ExecutorService ex = Executors.newFixedThreadPool(2);
		for(int i=0; i<ITERATION; i++)
		{
			ex.submit(new Runnable() {
				
				@Override
				public void run() {
					try {
						sendRequestCodec(1);
						Thread.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
		}
		ex.shutdown();
		try {
			ex.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			
		}
	}
	static String timeString(long t)
	{
		long min = TimeUnit.MILLISECONDS.toMinutes(t);
		t = t - TimeUnit.MINUTES.toMillis(min);
		long secs = TimeUnit.MILLISECONDS.toSeconds(t);
		t = t - TimeUnit.SECONDS.toMillis(secs);
		
		return (min+" mins "+secs+" secs "+t+" ms");
	}

}
