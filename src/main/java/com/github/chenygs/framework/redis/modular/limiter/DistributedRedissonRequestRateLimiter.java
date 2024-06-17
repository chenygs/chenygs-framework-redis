package com.github.chenygs.framework.redis.modular.limiter;

import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;

import java.lang.annotation.*;

/**
 * 基于redisson的限流注解  默认  每秒限制允许放行10个请求  缺点：无法针对指定ip或者指定人等进行限流
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DistributedRedissonRequestRateLimiter {
    String limiterName() default "";

    RateType mode() default RateType.PER_CLIENT;

    long rate() default 10;

    long rateInterval() default 1;

    RateIntervalUnit rateIntervalUnit() default RateIntervalUnit.SECONDS;
}
