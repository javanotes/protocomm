package com.reactiva.emulator.xcomm;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.RejectedExecutionHandler;
import io.netty.util.concurrent.SingleThreadEventExecutor;

class ConcurrentEventExecutor extends SingleThreadEventExecutor {

	private ExecutorService fj;
	
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
		fj = Executors.newFixedThreadPool(execThreads, new ThreadFactory() {
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
				fj.shutdown();
				try {
					fj.awaitTermination(10, TimeUnit.SECONDS);
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
            	fj.submit(new Runnable() {
					
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
