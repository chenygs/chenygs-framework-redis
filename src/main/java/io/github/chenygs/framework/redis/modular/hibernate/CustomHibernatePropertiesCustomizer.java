package io.github.chenygs.framework.redis.modular.hibernate;

import org.hibernate.cfg.AvailableSettings;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;

import java.util.Map;

public class CustomHibernatePropertiesCustomizer implements HibernatePropertiesCustomizer {
    private final RedissonClient redissonClient;

    public CustomHibernatePropertiesCustomizer(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
//        Properties p = new Properties();
//        p.forEach((key, val) -> {
//            hibernateProperties.put((String) key, val);
//        });
        hibernateProperties.put(AvailableSettings.CACHE_REGION_FACTORY, new New2RedisRegionFactory(redissonClient));
        hibernateProperties.put("hibernate.cache.redisson.entity.eviction.max_entries", "10000");
        hibernateProperties.put("hibernate.cache.redisson.entity.expiration.time_to_live", "86400000");
        hibernateProperties.put("hibernate.cache.redisson.entity.expiration.max_idle_time", "43200000");
//        hibernateProperties.put("hibernate.cache.redisson.imv3:.im_secret_message.expiration.max_entries", "1000");
//        hibernateProperties.put("hibernate.cache.redisson.imv3:.im_secret_message.expiration.time_to_live", "6000");
//        hibernateProperties.put("hibernate.cache.redisson.imv3:.im_secret_message.expiration.max_idle_time", "3000");
    }
}
