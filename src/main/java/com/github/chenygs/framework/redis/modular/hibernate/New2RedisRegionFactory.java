package com.github.chenygs.framework.redis.modular.hibernate;

import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;
import org.redisson.hibernate.RedissonRegionFactory;

import java.util.Map;

/**
 * @author chenygs
 * @date 2023/3/7 15:38
 */
@AllArgsConstructor
public class New2RedisRegionFactory extends RedissonRegionFactory{
    private static final long serialVersionUID = 8887392285596501033L;

    private RedissonClient redissonClient;

    @Override
    protected RedissonClient createRedissonClient(Map properties) {
        return redissonClient;
    }
}
