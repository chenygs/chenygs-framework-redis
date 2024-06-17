package com.github.chenygs.framework.redis.modular.limiter;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;

@Aspect
@Component
@Slf4j
public class RedissonRequestRateLimiterAspect {

    @Resource
    private RedissonClient redissonClient;

    @Around(value = "execution(* *(..)) && @annotation(rateLimiter)")
    public Object aroundMethod(ProceedingJoinPoint proceedingJoinPoint, DistributedRedissonRequestRateLimiter rateLimiter) throws Throwable {
        //没有添加限流注解的方法直接放行
        if (ObjectUtils.isEmpty(rateLimiter)) {
            return proceedingJoinPoint.proceed();
        }

        String limiterName = rateLimiter.limiterName();
        long rate = rateLimiter.rate();
        long rateInterval = rateLimiter.rateInterval();
        RateIntervalUnit rateIntervalUnit = rateLimiter.rateIntervalUnit();
        RateType mode = rateLimiter.mode();
        RRateLimiter rRateLimiter = redissonClient.getRateLimiter(limiterName);
        rRateLimiter.trySetRate(mode, rate, rateInterval, rateIntervalUnit);
        if (rRateLimiter.tryAcquire()) {
            return proceedingJoinPoint.proceed();
            // 限流了则返回
        } else {
            throw new RuntimeException("系统繁忙，请稍后重试！");
        }
    }
}
