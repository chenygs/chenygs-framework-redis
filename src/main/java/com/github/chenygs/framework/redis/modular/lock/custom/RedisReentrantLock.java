package com.github.chenygs.framework.redis.modular.lock.custom;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class RedisReentrantLock implements Lock, java.io.Serializable {

    private StringRedisTemplate stringRedisTemplate;
    private String uniqueLock;
    private String unique_id;

    //默认过期时间 单位秒
    private long expire = 30;

    public RedisReentrantLock(StringRedisTemplate stringRedisTemplate, String unique_id, String uniqueLock) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.uniqueLock = uniqueLock;
        this.unique_id = unique_id + ":" + Thread.currentThread().getId();
    }

    @Override
    public void lock() {
        this.tryLock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        try {
            return this.tryLock(-1L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        if (time != -1) {
            this.expire = unit.toSeconds(time);
        }
        String lua_script =
                "if redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1],ARGV[1]) == 1 " +
                        "then  " +
                        " redis.call('hincrby', KEYS[1],ARGV[1],1) " +
                        " redis.call('expire', KEYS[1],ARGV[2]) " +
                        " return 1 " +
                        "else  " +
                        " return 0 " +
                        "end";

        Boolean lock = stringRedisTemplate.execute(new DefaultRedisScript<>(lua_script, Boolean.class), Arrays.asList(uniqueLock), unique_id, String.valueOf(expire));
        while (!lock) {
            Thread.sleep(20);
        }
        //加锁成功后，开启定时器，续期
        autoRenew();
        return true;
    }

    @Override
    public void unlock() {
        String lua_script =
                "if redis.call('hexists',KEYS[1],ARGV[1]) == 0 " +
                        "then " +
                        " return nil " +
                        "elseif redis.call('hincrby',KEYS[1],ARGV[1],-1) == 0 " +
                        "then  " +
                        " return redis.call('del',ARGV[1]) " +
                        "else  " +
                        " return 0 " +
                        "end";
        Long release = stringRedisTemplate.execute(new DefaultRedisScript<>(lua_script, Long.class), Arrays.asList(uniqueLock), unique_id);
        if (release == null) {
            throw new IllegalMonitorStateException("恶意释放锁!!!");
        }

    }

    @Override
    public Condition newCondition() {
        return null;
    }

    //自动续期
    private void autoRenew() {
        String lua_script =
                "if redis.call('hexists',KEYS[1],ARGV[1]) == 1 " +
                        "then " +
                        " return redis.call('expire',KEYS[1],ARGV[2]) " +
                        "else  " +
                        " return 0 " +
                        "end";
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Boolean renew = stringRedisTemplate.execute(new DefaultRedisScript<>(lua_script, Boolean.class), Arrays.asList(uniqueLock), unique_id, String.valueOf(expire));
                if (renew) {//如果重置成功
                    autoRenew();
                }
            }
        }, this.expire * 1000 / 3);
    }
}
