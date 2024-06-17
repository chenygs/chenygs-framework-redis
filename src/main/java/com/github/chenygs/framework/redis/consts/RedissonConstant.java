package com.github.chenygs.framework.redis.consts;

/**
 * @author chenygs
 * @description 缓存相关常量类
 * @version V3.0
 **/
public class RedissonConstant {
    // 冒号
    public static final String COLON = ":";
    // 横杠
    public static final String CROSSBAR = "-";
    // 锁前缀
    public static final String LOCK = "lock:";
    // ouyunc 公共前缀
    public static final String COMMON_PREFIX = "chenygs-boots:";
    // 集群服务下线hash key
    public static final String CLUSTER_SERVER = "cluster:server:";
}
