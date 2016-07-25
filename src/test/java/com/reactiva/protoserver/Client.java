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

public class Client {

	static void sendRequest(String msg) throws UnknownHostException, IOException
	{
		Socket s = new Socket("localhost", port);
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
	
	static void sendRequests(String msg) throws UnknownHostException, IOException
	{
		Socket s = new Socket("localhost", port);
		try {
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			DataInputStream in = new DataInputStream(s.getInputStream());
			for (int i = 0; i < cycle; i++) {
				
				byte[] req = (msg + "Request# "+i).getBytes(StandardCharsets.UTF_8);
				out.writeInt(req.length + 4);
				out.write(req);
				out.flush();
				System.out.println("Client => " + msg + "\t" + in.readUTF());
			}
		} finally {
			s.close();
		}
	}
	
	static int iter = 10, port = 8081, concurrency = 4, cycle = 100;
	public static void main(String[] args) throws UnknownHostException, IOException {
		ExecutorService ex = Executors.newFixedThreadPool(2);
		for (int i = 0; i < concurrency; i++) {
			ex.submit(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < iter; i++) {
						try {
							//sendRequest("HELLO SOMOS " + i);
							sendRequests("HELLO SOMOS " + i);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				}
			});
		}
		ex.shutdown();
		try {
			ex.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			
		}
	}

}
