/* ============================================================================
*
* FILE: FixedLengthProtocolHandler.java
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
/**
 * A base class for handling fixed length byte messages. These type of streams 
 * would specify the total number of bytes to read, in their initial byte/s.
 */
public abstract class FixedLengthProtocolHandler extends AbstractProtocolHandler {

  public FixedLengthProtocolHandler() {
    super();
  }

  private int lengthOffset = INT_OFFSET;
  protected int length = -1;
  
  /**
   * Extract the length of data bytes based on the length offset numeric type.
   * Say for an {@linkplain ProtocolHandler#INT_OFFSET int offset}, the first 4 bytes
   * would give the length of data bytes.
   */
  protected void extractLength()
  {
    if(totalRead >= lengthOffset)
    {
      switch (lengthOffset) 
      {
        case BYTE_OFFSET:
          length = ByteBuffer.wrap(readBuffer.array(), 0, lengthOffset).order(ByteOrder.BIG_ENDIAN).get()+lengthOffset;
          break;
        case SHORT_OFFSET:
          length = ByteBuffer.wrap(readBuffer.array(), 0, lengthOffset).order(ByteOrder.BIG_ENDIAN).getShort()+lengthOffset;
          break;
        case INT_OFFSET:
          length = ByteBuffer.wrap(readBuffer.array(), 0, lengthOffset).order(ByteOrder.BIG_ENDIAN).getInt()+lengthOffset;
          break;
        case LONG_OFFSET:
          length = (int) (ByteBuffer.wrap(readBuffer.array(), 0, lengthOffset).order(ByteOrder.BIG_ENDIAN).asLongBuffer().get()+lengthOffset);
          break;
  
        default:
          break;
      }
    }
  }
  //A protocol handler expecting an int mentioning total size, then int, a double, then an int
  //mentioning the size of an array, and finally the string as char array.
  // so a data '5|2.5|hello' will have a total length of (4 + 8 + 4 + (2*5)) = 26 bytes.
  // in formatted, the first 4 bytes will hold the integer 26.
  @Override
  public boolean doRead(SocketChannel channel) throws IOException
  {
    readBuffer.clear();
    read = channel.read(readBuffer);
            
    if (read > 0) 
    {
      totalRead += read;
      
      if(length == -1)
      {
        extractLength();
      }
      if(totalRead > length)
      {
        throw new IOException("Expected bytes: "+length+"\tGot bytes: "+totalRead);
      }
      readBuffer.flip();
      bufferedRead(Arrays.copyOfRange(readBuffer.array(), 0, read));
      return totalRead == length;
    }
    
    return false;
                
  }
  /**
   * To be overridden for other buffering logic. Need to override {@link #getReadStream()} as well.
   * @param nextBytes
   * @throws IOException
   */
  protected void bufferedRead(byte[] nextBytes) throws IOException
  {
	 writeStream.write(nextBytes);
  }
  @Override
  public InputStream getReadStream() {
    return new ByteArrayInputStream(writeStream.toByteArray());
  }
  public int getLengthOffset() {
    return lengthOffset;
  }
  /**
   * The offset which contains the length of byte to read. Defaults to {@linkplain ProtocolHandler#INT_OFFSET}.
   * That means the first 4 bytes would contain the length.
   * @param lengthOffset
   */
  public void setLengthOffset(int lengthOffset) {
    this.lengthOffset = lengthOffset;
  }


}
