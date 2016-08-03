package com.reactiva.protoserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.smsnow.adaptation.dto.LogoffRequest;
import com.smsnow.adaptation.protocol.CodecException;
import com.smsnow.adaptation.protocol.itoc.StreamedITOCCodec;

public class ProtocolConvTests {

	private static StreamedITOCCodec codec = new StreamedITOCCodec();
	public static void main(String[] args) throws CodecException {
		LogoffRequest lr = new LogoffRequest();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		codec.encode(lr, new DataOutputStream(out));
		lr = codec.decode(LogoffRequest.class, new DataInputStream(new ByteArrayInputStream(out.toByteArray())));

	}

}
