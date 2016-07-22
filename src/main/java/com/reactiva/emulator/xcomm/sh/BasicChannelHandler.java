package com.reactiva.emulator.xcomm.sh;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * This is a monolithic implementation of request processing.
 * Kept for backward compatibility/simplicity, and to be understood as compared to the DV phase
 * development code.<p>
 * To use override {@link #doProcess(DataInputStream, DataOutputStream)} method.
 * 
 * @author sutanu
 *
 */
@Sharable
@Deprecated
public class BasicChannelHandler extends ChannelInboundHandlerAdapter {

	private static final Logger log = LoggerFactory.getLogger(BasicChannelHandler.class);
	
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception { // (2)
		ByteBuf buf = (ByteBuf) msg;
		try 
        {
            byte[] in = new byte[buf.readableBytes()];
        	buf.readBytes(in);
        	
        	ByteArrayOutputStream out = new ByteArrayOutputStream();
        	doProcess(new DataInputStream(new ByteArrayInputStream(in)), new DataOutputStream(out));
        	
        	out.flush();
        	in = out.toByteArray();
        	
        	buf = ctx.alloc().heapBuffer(in.length);
        	        	
        	try {
        		buf.writeBytes(in);
				ctx.writeAndFlush(buf).addListener(ChannelFutureListener.CLOSE);
			} finally {
				if(buf.refCnt() > 0)
					ReferenceCountUtil.release(buf);
			}
        	
        	
        } finally {
            ReferenceCountUtil.release(msg);
            
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        log.error("Closing client connection on exception", cause);
        ctx.close();
    }
    
	/**
	 * Execute your service, and set response.
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected void doProcess(DataInputStream request, DataOutputStream response) throws Exception {
		/*int len = request.readInt();
		byte[] b = new byte[len];
		request.readFully(b);
		
		String respStr = "Got request: "+new String(b, StandardCharsets.UTF_8);
		
		response.writeUTF(respStr);*/
		
	}

}
