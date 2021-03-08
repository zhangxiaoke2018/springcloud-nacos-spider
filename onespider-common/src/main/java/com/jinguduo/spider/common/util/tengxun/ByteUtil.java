package com.jinguduo.spider.common.util.tengxun;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ByteUtil {
    public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
    public static final String STRING_UTF8 = "UTF-8";

    public static final byte[] fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value.getBytes();
        }
    }

    public static boolean isEmpty(byte[] bytes) {
        return bytes == null || bytes.length == 0;
    }

    public static final ByteBuffer toByteBuffer(byte[] bytes) {
        return bytes == null ? null : ByteBuffer.wrap(bytes);
    }

    public static final ByteBuffer toByteBuffer(String value) {
        byte[] bytes = fromString(value);
        return bytes == null ? null : ByteBuffer.wrap(bytes);
    }

    public static byte[] toBytes(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }
        byte[] bytes = new byte[(byteBuffer.limit() - byteBuffer.position())];
        try {
            byteBuffer.get(bytes);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return bytes;
        }
    }

    public static final String toString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }

    public static String toString(ByteBuffer name) {
        return toString(toBytes(name));
    }
}
