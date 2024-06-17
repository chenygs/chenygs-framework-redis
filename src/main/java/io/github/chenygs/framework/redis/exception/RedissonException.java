package io.github.chenygs.framework.redis.exception;

public class RedissonException extends RuntimeException {
    private String message;

    public RedissonException(String message) {
        super(message);
    }
}
