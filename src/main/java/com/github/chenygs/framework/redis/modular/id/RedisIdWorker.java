package com.github.chenygs.framework.redis.modular.id;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author chenygs
 * 分布式全局唯一id生成策略  64字节
 * 1、符号位  1bit  永远是0
 * 2、时间戳  31bit  以秒为单位可以使用69年
 * 3、序列号  32bit  单秒可以产生2^32 不同的序列号
 * redis increment 指令自增上限2^64
 * @date 2023/2/3 14:53
 */
@Component
public class RedisIdWorker {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private static final long BEGIN_TIMESTAMP = LocalDateTime.of(2022, 1, 1, 0, 0, 0).toEpochSecond(ZoneOffset.UTC);

    private static final int COUNT_BITS = 32;

    public long nextId(String prefix) {
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        //时间戳生成 可以防止redis清空的情况
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        //序列号生成
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd");
        String date = now.format(timeFormatter);
        Long increment = stringRedisTemplate.opsForValue().increment("business:" + prefix + ":" + date);
        // 0 - 00000000 00000000 00000000 00000000 - 00000000 00000000 00000000 00000000
        // 上述时间戳和序列号拼接 字符串拼接效率不高
        // 将 timestamp  左移32位 再与 increment 或运算
        return timestamp << COUNT_BITS | increment;
    }
}
