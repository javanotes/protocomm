package com.smsnow.adaptation.server.pipe;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.springframework.util.Assert;

import com.smsnow.adaptation.protocol.BufferedLengthBasedCodec;
import com.smsnow.adaptation.protocol.CodecException;
import com.smsnow.adaptation.protocol.itoc.ITOCCodecWrapper;
import com.smsnow.adaptation.server.RequestHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
@Sharable
public class ResponseConvertorHandler extends MessageToByteEncoder<Serializable> {

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
	protected void write(Serializable resp, DataOutputStream out) throws IOException, CodecException
	{
		Assert.isAssignable(resp.getClass(), rh.responseMapping());
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
			write(msg, new DataOutputStream(bytes));
			out.writeBytes(bytes.toByteArray());
		}
	}

}
