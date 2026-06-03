package com.campus.seat.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisLockService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisLockService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(String key, long timeoutSeconds) {
        try {
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(key, "LOCKED", timeoutSeconds, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return true;
        }
    }

    public void unlock(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            // Redis unavailable, lock will auto-expire
        }
    }
}
