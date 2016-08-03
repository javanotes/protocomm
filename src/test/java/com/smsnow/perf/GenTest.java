package com.smsnow.perf;

import java.util.Arrays;

import com.smsnow.adaptation.protocol.gen.CodeGen;

public class GenTest {

	public static void main(String[] args) {
		
		CodeGen.run(Arrays.asList("-t", "C:\\Users\\esutdal\\Documents\\SOMOS\\codegen\\template.txt", "-c", "ITOCRequest", /*"-i", "req",*/ "-s", "1", "-d",
				"C:\\Users\\esutdal\\Documents\\SOMOS\\codegen\\", "-p", "com.smsnow.adaptation.server.dto"));

	}

}
