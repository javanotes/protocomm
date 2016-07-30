package com.reactiva.emulator.xcomm.sh;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.smsnow.protocol.CodecException;
import com.smsnow.protocol.IType;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
@Sharable
public class ResponseConvertorHandler extends MessageToByteEncoder<IType> {

	ITOCCodecHandler codecHdlr;
	public ResponseConvertorHandler(ITOCCodecHandler codecHdlr) {
		this.codecHdlr = codecHdlr;
	}
	/**
	 * Write the response to out stream.
	 * @param resp
	 * @param out
	 * @throws IOException 
	 * @throws CodecException 
	 */
	protected void write(IType resp, DataOutputStream out) throws IOException, CodecException
	{
		codecHdlr.write(resp, out);
	}
	@Override
	protected void encode(ChannelHandlerContext ctx, IType msg, ByteBuf out) throws Exception {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		write(msg, new DataOutputStream(b));
		out.writeBytes(b.toByteArray());
	}

}
