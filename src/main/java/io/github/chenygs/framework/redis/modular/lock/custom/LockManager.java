package io.github.chenygs.framework.redis.modular.lock.custom;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

@Component
public class LockManager {

    private String unique_id;

    public LockManager() {
        this.unique_id = UUID.randomUUID().toString();
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public RedisReentrantLock getRedisLock(String uniqeLock) {
        return new RedisReentrantLock(stringRedisTemplate, unique_id, uniqeLock);
    }
}
