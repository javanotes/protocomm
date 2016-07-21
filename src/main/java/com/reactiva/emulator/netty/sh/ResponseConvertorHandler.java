package com.reactiva.emulator.netty.sh;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.reactiva.emulator.netty.dto.Response;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;
@Sharable
public class ResponseConvertorHandler extends MessageToByteEncoder<Response> {

	/**
	 * Write the response to out stream.
	 * @param resp
	 * @param out
	 * @throws IOException 
	 */
	protected void write(Response resp, DataOutputStream out) throws IOException
	{
		//TODO override
		out.writeUTF(resp.getPayload());
	}
	@Override
	protected void encode(ChannelHandlerContext ctx, Response msg, ByteBuf out) throws Exception {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		write(msg, new DataOutputStream(b));
		out.writeBytes(b.toByteArray());
	}

}
