package com.smsnow.adaptation.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class CommonUtil {

	public static byte[] intToBytes(int i)
	{
		return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(i).array();
	}
	
	public static byte[] padBytes(byte[] origBytes, int targetLen, byte padByte)
	{
		int len = origBytes.length;
		byte[] targetBytes = Arrays.copyOf(origBytes, targetLen);
		if(targetLen > len)
		{
			for(int i=len; i<targetLen; i++)
			{
				targetBytes[i] = padByte;
			}
		}
		
		return targetBytes;
	}
}
