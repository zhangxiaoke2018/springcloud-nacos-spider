package com.jinguduo.spider.repo;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class CompressedTimeStampSerializer implements RedisSerializer<Long> {

	final static Charset CHARSET = StandardCharsets.UTF_8;
	final static int RADIX = Character.MAX_RADIX;
	
	// 从 2018-05-30 00:00:00.000 开始纪元(milliseconds)
	final static long EPOCH = 1527638400000L;
	
	@Override
	public byte[] serialize(Long t) throws SerializationException {
		if (t == null) {
			return null;
		}
		// 精确到 Seconds，而不是 Millis
		long v = (long)((t - EPOCH) / 1000);
		String s = Long.toString(v, RADIX);
		return s.getBytes(CHARSET);
	}

	@Override
	public Long deserialize(byte[] bytes) throws SerializationException {
		if (bytes == null) {
			return null;
		}
		String s = new String(bytes, CHARSET);
		long v = Long.parseLong(s, RADIX);
		return ((v * 1000) + EPOCH);
	}

}
