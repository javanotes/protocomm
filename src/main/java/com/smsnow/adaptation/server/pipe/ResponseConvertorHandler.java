package com.smsnow.adaptation.server.pipe;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.smsnow.adaptation.protocol.CodecException;
import com.smsnow.adaptation.protocol.itoc.ITOCCodecWrapper;
import com.smsnow.adaptation.server.RequestHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
@Sharable
public class ResponseConvertorHandler extends MessageToByteEncoder<Serializable> {

	private static final Logger log = LoggerFactory.getLogger(ResponseConvertorHandler.class);
	private ITOCCodecWrapper codec;
	private RequestHandler rh;
	private boolean buffCodec;
	/**
	 * 
	 * @param codecHdlr
	 * @param rh
	 * @param buffCodec
	 */
	public ResponseConvertorHandler(ITOCCodecWrapper codecHdlr, RequestHandler rh, boolean buffCodec) {
		this.codec = codecHdlr;
		this.rh = rh;
		this.buffCodec = buffCodec;
	}
	/**
	 * Write the response to out stream.
	 * @param resp
	 * @param out
	 * @throws IOException 
	 * @throws CodecException 
	 */
	protected void write(Serializable resp, DataOutputStream out) throws CodecException
	{
		codec.encode(resp, out);
	}
	protected ByteBuffer write(Serializable resp) throws IOException, CodecException
	{
		Assert.isAssignable(resp.getClass(), rh.responseMapping());
		return codec.encode(resp);
	}
	@Override
	protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
		if (buffCodec) {
			out.writeBytes(write(msg));
		}
		else
		{
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			log.debug("Writing response of type "+msg.getClass());
			try {
				write(msg, new DataOutputStream(bytes));
			} catch (CodecException e) {
				log.error("--Response conversion error--", e);
			}
			out.writeBytes(bytes.toByteArray());
			
		}
	}

}
