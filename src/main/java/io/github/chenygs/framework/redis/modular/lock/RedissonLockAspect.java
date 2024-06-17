package io.github.chenygs.framework.redis.modular.lock;

import io.github.chenygs.framework.redis.consts.RedissonConstant;
import io.github.chenygs.framework.redis.exception.RedissonException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * redis分布式锁切面
 *
 * @author chenygs
 * @date 2023/2/7 14:26
 */
@Aspect
@Slf4j
@Component
public class RedissonLockAspect {
    @Resource
    private RedissonClient redissonClient;

    /**
     * @param proceedingJoinPoint
     * @param distributedRedissonLock
     * @description 环绕通知，定义redis切面,不定义切点
     * 注意：由于aspectj maven编译器有bug 在调用方也织入了，所以 使用execution(* *(..)) 和 @annotation(myRedisLock)两个配合使用
     **/
    @Around(value = "execution(* *(..)) && @annotation(distributedRedissonLock)")
    public Object aroundMethod(ProceedingJoinPoint proceedingJoinPoint, DistributedRedissonLock distributedRedissonLock) throws Throwable {
        //获得当前线程名称
        String currentThreadName = Thread.currentThread().getName();
        //获取redis锁的名称
        String name = distributedRedissonLock.name();
        //判断锁是否有值
        if (!StringUtils.hasLength(name)) {
            //生成锁键名称 lock key name
            MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = proceedingJoinPoint.getArgs();
            name = RedissonConstant.COMMON_PREFIX + RedissonConstant.LOCK + signature.toLongString();
            List<Integer> argsHashCode = new ArrayList<>();
            for (int i = 0; i < args.length; i++) {
                argsHashCode.add(args[i].hashCode());
            }
            Collections.sort(argsHashCode);
            // 将有序参数map进行拼接处理
            String[] nameSplit = name.split("[(|)]");
            if (nameSplit != null && nameSplit.length > 1) {
                name = nameSplit[0] + "(";
                for (int i = 0; i < argsHashCode.size(); i++) {
                    name = name + argsHashCode.get(i);
                    if (i < argsHashCode.size() - 1) {
                        name = name + ",";
                    } else {
                        name = name + ")";
                    }
                }
            }
        }
        log.info("线程：{} 开始获取分布式锁name: {}", currentThreadName, name);
        //获取锁
        RLock lock = redissonClient.getLock(name);
        try {
            //获取锁的最长等待时间，以秒为单位
            long waitTime = distributedRedissonLock.waitTime();
            //锁的超时间(有效时间)
            long leaseTime = distributedRedissonLock.leaseTime();
            //如果没指定超时时间(包括等待超时时间和锁的超时时间)则直接上锁
            if (waitTime == 0 && leaseTime == 0) {
                //加锁 锁的有效期默认30秒
                lock.lock();
            }
            //如果指定了获取锁的等待超时间和锁的过期时间则尝试上锁
            if (waitTime != 0 && leaseTime != 0) {
                //如果没有获取锁的，抛出异常
                if (!lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                    throw new RedissonException(name + " 获取锁失败");
                }
            }
            //如果没有指定了获取锁的等待时间，但指定锁的有效时间
            if (waitTime == 0 && leaseTime != 0) {
                lock.lock(leaseTime, TimeUnit.SECONDS);
            }
            //如果指定了获取锁的等待时间，但没有指定锁的有效时间
            if (waitTime != 0 && leaseTime == 0) {
                lock.tryLock(waitTime, TimeUnit.SECONDS);
            }
            log.info("线程：" + currentThreadName + "获取分布式锁完成");
            //执行目标方法
            return proceedingJoinPoint.proceed();
        } catch (Exception e) {
            log.error("线程" + currentThreadName + "获取redis锁失败！");
            throw e;
        } finally {
            //释放锁
            lock.unlock();
        }
    }
}
