package com.smsnow.adaptation.server.pipe;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.springframework.util.Assert;

import com.smsnow.adaptation.server.RequestHandler;
import com.smsnow.protocol.CodecException;
import com.smsnow.protocol.FixedLenCodec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
@Sharable
public class ResponseConvertorHandler extends MessageToByteEncoder<Serializable> {

	private FixedLenCodec codec;
	private RequestHandler rh;
	public ResponseConvertorHandler(FixedLenCodec codecHdlr, RequestHandler rh) {
		this.codec = codecHdlr;
		this.rh = rh;
	}
	/**
	 * Write the response to out stream.
	 * @param resp
	 * @param out
	 * @throws IOException 
	 * @throws CodecException 
	 */
	protected void write(Serializable resp, DataOutputStream out) throws IOException, CodecException
	{
		Assert.isAssignable(resp.getClass(), rh.responseMapping());
		codec.encode(resp, out);
	}
	@Override
	protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		write(msg, new DataOutputStream(b));
		out.writeBytes(b.toByteArray());
	}

}
