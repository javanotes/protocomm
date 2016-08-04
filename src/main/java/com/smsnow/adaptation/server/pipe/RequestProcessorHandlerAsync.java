package com.smsnow.adaptation.server.pipe;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smsnow.adaptation.dto.ITOCRequest;
import com.smsnow.perf.ITOCLogin;
import com.smsnow.perf.ITOCLoginResponse;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

/**
 * Asynchronous request processing prototype. This needs to be worked
 * upon for generating async responses back to the client. Care must be taken
 * that the core executor threads of Netty do not get blocked as that will hit
 * the scalability and overall performance. 
 * <p>
 * Ideally we should be able to find
 * out some way to intelligently utilise the Netty executor threads. If not possible
 * then we need to create an extra thread pooling of our own.
 * @author esutdal
 *
 */
@Sharable
public class RequestProcessorHandlerAsync extends ChannelInboundHandlerAdapter {

	private static final Logger log = LoggerFactory.getLogger(RequestProcessorHandlerAsync.class);
	static final String POISON_PILL = "POISON_PILL";
	
	/*
	 * These are the netty executor threads. 
	 * Set the concurrent event executor group.
	 */
	DefaultEventExecutorGroup exec;
	/**
	 * This is the correlation between the request and response.
	 * The async worker thread would poll/query the cache 
	 * for the correlation ID and return the response.
	 * @author esutdal
	 *
	 */
	private static class CorrelatedRequest implements Serializable
	{

		public CorrelatedRequest(ChannelHandlerContext context, ITOCRequest request) {
			super();
			this.context = context;
			this.request = request;
		}
		final ChannelHandlerContext context;
		final ITOCRequest request;
		String correlationID;
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
	public RequestProcessorHandlerAsync()
	{
		
	}
	@PreDestroy
	private void stopThread()
	{
		queue.offer(POISON_PILL);
	}
	private BlockingQueue<Serializable> queue;
	@PostConstruct
	private void init()
	{
		queue = new ArrayBlockingQueue<>(100);
		new Thread("xcomm-exec-async")
		{
			@Override
			public void run()
			{
				log.info("Started async request processor thread");
				while(true)
				{
					try 
					{
						Serializable next = queue.take();
						if(POISON_PILL.equals(next))
						{
							break;
						}
						else
						{
							CorrelatedRequest req = (CorrelatedRequest) next;
							//do some process in a separate thread and then commit
							//submitToIMSQueue
							
							//probably this thread should only poll the cache to see
							//if any response is made available and return it.
							ITOCLogin login = (ITOCLogin) req.request;
							log.info("Login request for user:: "+login.getUserLogonID()+" password:: "+login.getUserPassword());
							ITOCLoginResponse resp = new ITOCLoginResponse();
							resp.setUserLogonID(login.getUserLogonID());
							resp.setLogonStatus("F");
							req.context.fireChannelRead(resp);
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}.start();
	}
			
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (acceptInboundMessage(msg)) {
                ITOCRequest cast = (ITOCRequest) msg;
                try {
                    decode(ctx, cast);
                } finally {
                    ReferenceCountUtil.release(cast);
                }
            } else {
                throw new DecoderException("Not an instance of "+ITOCRequest.class);
            }
        } catch (DecoderException e) {
            throw e;
        } catch (Exception e) {
            throw new DecoderException(e);
        } finally {
            
        }
    }
	
	private boolean acceptInboundMessage(Object msg) {
		return msg instanceof ITOCRequest;
	}
	private void decode(ChannelHandlerContext ctx, ITOCRequest msg) throws Exception {
		submitToProcessQueue(msg);
		queue.offer(new CorrelatedRequest(ctx, msg));
	}
	/**
	 * The method which prepares a MQ request and submits to a 
	 * corresponding queue based on the request type.
	 * @param msg
	 */
	protected void submitToProcessQueue(ITOCRequest msg) {
		// TODO Implement
		log.info("Submitted to process queue");
	}

}
