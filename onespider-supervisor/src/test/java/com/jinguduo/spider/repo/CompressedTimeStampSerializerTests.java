package com.jinguduo.spider.repo;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class CompressedTimeStampSerializerTests {
	
	public void testBytesToInt() {
	}
	
	@Test
	public void testIntToByte() {
		byte[] bytes = intToByteArray(1024);
		System.out.println(new String(bytes));
	}
	
	//byte 数组与 int 的相互转换  
	public static int byteArrayToInt(byte[] b) {  
	    return   b[3] & 0xFF |  
	            (b[2] & 0xFF) << 8 |  
	            (b[1] & 0xFF) << 16 |  
	            (b[0] & 0xFF) << 24;  
	}  
	  
	public static byte[] intToByteArray(int a) {  
	    return new byte[] {  
	        (byte) ((a >> 24) & 0xFF),  
	        (byte) ((a >> 16) & 0xFF),     
	        (byte) ((a >> 8) & 0xFF),     
	        (byte) (a & 0xFF)  
	    };  
	}
	
	private static ByteBuffer buffer = ByteBuffer.allocate(8);

	// byte 数组与 long 的相互转换
	public static byte[] longToBytes(long x) {
		buffer.putLong(0, x);
		return buffer.array();
	}

	public static long bytesToLong(byte[] bytes) {
		buffer.put(bytes, 0, bytes.length);
		buffer.flip();// need flip
		return buffer.getLong();
	}
}
