package com.reactiva.protoserver;

import java.util.Arrays;

import com.smsnow.protocol.gen.Generator;

public class GenTest {

	public static void main(String[] args) {
		
		Generator.run(Arrays.asList("-t", "C:\\workspace\\template.txt", "-c", "ITOCLogin", "-s", "1", "-d",
				"C:\\workspace", "-p", "com.somos.smsnow"));

	}

}
