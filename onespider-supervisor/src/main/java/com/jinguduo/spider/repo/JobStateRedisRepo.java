package com.jinguduo.spider.repo;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.common.constant.FrequencyConstant;

@Component
public class JobStateRedisRepo implements InitializingBean {

    @Autowired
    private JedisConnectionFactory jedisConnectionFactory;
    
    private RedisTemplate<String, Long> redisTemplate;
    
    private final static long DEFAULT_EXPIRE_SECONDS = FrequencyConstant.DEFAULT;
    
    public Long findJobStateByKey(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return redisTemplate.opsForValue().get(key);
    }

    public void saveJobState(Job job) {
        Assert.notNull(job, "The Job must can't null");
        Assert.notNull(job.getId(), "The Job.Id must can't null");
        long expire = DEFAULT_EXPIRE_SECONDS;
        if (job.getFrequency() != null && job.getFrequency() > 0) {
            expire = job.getFrequency().longValue();
        }
        redisTemplate.opsForValue().set(job.getId(), job.getCrawledAt(), expire, TimeUnit.SECONDS);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        RedisSerializer<Long> valueSerializer = new CompressedTimeStampSerializer();
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.afterPropertiesSet();
    }

}
