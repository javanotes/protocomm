package com.smsnow.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.smsnow.adaptation.dto.ApplicationHeader;
import com.smsnow.adaptation.protocol.CodecException;
import com.smsnow.adaptation.protocol.ProtoConfig;
import com.smsnow.adaptation.protocol.itoc.StreamedITOCCodec;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ProtoConfig.class})
public class ProtocolConvTests {

	@Autowired
	private StreamedITOCCodec codec;
	@Test
	public void testCodecAsBlackBox()
	{
		try {
			LogoffResponse lr = new LogoffResponse();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			codec.encode(lr, new DataOutputStream(out));
			lr = codec.decode(LogoffResponse.class, new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
		} catch (CodecException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}
	@Test
	public void testCodecOnThisFieldLevel()
	{
		try {
			LogoffResponse lr = new LogoffResponse();
			lr.setLogoffStatus("X");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			codec.encode(lr, new DataOutputStream(out));
			lr = codec.decode(LogoffResponse.class, new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
			Assert.assertEquals("X", lr.getLogoffStatus());
		} catch (CodecException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}
	@Test
	public void testCodecOnSuperFieldLevel()
	{
		try {
			LogoffResponse lr = new LogoffResponse();
			lr.setApplicationHeader(new ApplicationHeader());
			lr.getApplicationHeader().setFiller11("XXXXXX");
			lr.setLogoffStatus("X");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			codec.encode(lr, new DataOutputStream(out));
			lr = codec.decode(LogoffResponse.class, new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
			Assert.assertEquals("X", lr.getLogoffStatus());
			Assert.assertEquals("XXXXXX", lr.getApplicationHeader().getFiller11());
		} catch (CodecException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}
	@Test
	public void testCodecOnSuperFieldLevelWithPaddedData()
	{
		try {
			LogoffResponse lr = new LogoffResponse();
			lr.setApplicationHeader(new ApplicationHeader());
			lr.getApplicationHeader().setFiller11("XXX");
			lr.setLogoffStatus("X");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			codec.encode(lr, new DataOutputStream(out));
			lr = codec.decode(LogoffResponse.class, new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
			Assert.assertEquals("X", lr.getLogoffStatus());
			Assert.assertEquals("XXX***", lr.getApplicationHeader().getFiller11());
		} catch (CodecException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}
	@Test
	public void testCodecOnThisFieldLevelWithTruncatedData()
	{
		try {
			LogoffResponse lr = new LogoffResponse();
			lr.setLogoffStatus("XXX");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			codec.encode(lr, new DataOutputStream(out));
			lr = codec.decode(LogoffResponse.class, new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
			Assert.assertEquals("X", lr.getLogoffStatus());
		} catch (CodecException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}
	@Test
	public void testCodecOnThisFieldLevelWithPaddedData()
	{
		try {
			LogoffRequest lr = new LogoffRequest();
			lr.setUserLogonID("XXXX");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			codec.encode(lr, new DataOutputStream(out));
			lr = codec.decode(LogoffRequest.class, new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
			Assert.assertEquals("XXXX****", lr.getUserLogonID());
		} catch (CodecException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}
	

}
