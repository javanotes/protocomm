/* ============================================================================
*
* FILE: ProtocolHandlerFactory.java
*
The MIT License (MIT)

Copyright (c) 2016 Sutanu Dalui

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*
* ============================================================================
*/
package com.reactiva.emulator.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Extend the factory to create custom handlers.
 * This class should be overriden.
 * 
 */
public class ProtocolHandlerFactory implements FactoryBean<ProtocolHandler>{

	private static Logger log = LoggerFactory.getLogger(ProtocolHandlerFactory.class);
  @Override
  public ProtocolHandler getObject()  {
    return new FixedLengthProtocolHandler(){
    	
		@Override
		protected void execute(DataInputStream in, DataOutputStream out) throws IOException {
			log.info("FixedLengthProtocolHandler() {...}.doProcess()");
    		int len = in.readInt();
    		byte[] b = new byte[len-4];
    		in.readFully(b);
    		String req = (new String(b, StandardCharsets.UTF_8));
    	    out.writeUTF("Processing request:: "+req);
			
		}
    };
  }

  @PostConstruct
  void init()
  {
	  log.info("ProtocolHandlerFactory.init()");
  }
  @Override
  public Class<?> getObjectType() {
    return ProtocolHandler.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

  
}
