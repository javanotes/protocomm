package com.reactiva.emulator.net;

public interface Server extends Runnable{

	/**
	   * Stops the server.
	   */
	void stopServer();

	/**
	   * Starts the server
	   */
	void startServer();

}