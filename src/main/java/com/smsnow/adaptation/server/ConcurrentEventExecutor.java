package com.smsnow.adaptation.server;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.RejectedExecutionHandler;
import io.netty.util.concurrent.SingleThreadEventExecutor;
/**
 * @deprecated Netty data structures are not synchronized.
 * @author esutdal
 *
 */
class ConcurrentEventExecutor extends SingleThreadEventExecutor {

	private ExecutorService executors;
	
	/**
	 * 
	 * @param parent
	 * @param executor
	 * @param maxPendingTasks
	 * @param rejectedExecutionHandler
	 * @param execThreads
	 */
	ConcurrentEventExecutor(EventExecutorGroup parent, Executor executor, int maxPendingTasks,
            RejectedExecutionHandler rejectedExecutionHandler, int execThreads) {
		super(parent, executor, true, maxPendingTasks, rejectedExecutionHandler);
		executors = Executors.newFixedThreadPool(execThreads, new ThreadFactory() {
			int n = 0;
			@Override
			public Thread newThread(Runnable arg0) {
				Thread t = new Thread(arg0, "xcomm-exec-"+(n++));
				return t;
			}
		});
		
		addShutdownHook(new Runnable() {
			
			@Override
			public void run() {
				executors.shutdown();
				try {
					executors.awaitTermination(10, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
	}
		
	private void run0(Runnable r)
	{
		if (r != null) {
            r.run();
            updateTime();
        }
	}
	private synchronized void updateTime() {
        updateLastExecutionTime();
    }	
	@Override
	protected void run() {
		
		for (;;) 
		{
            final Runnable task = takeTask();
            if (task != null) {
            	executors.submit(new Runnable() {
					
					@Override
					public void run() {
						run0(task);
					}
				});
                
            }

            if (confirmShutdown()) {
                break;
            }
        }
		
	}

}
