package com.github.chenygs.framework.redis.config;

import com.github.chenygs.framework.redis.modular.hibernate.CustomHibernatePropertiesCustomizer;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chenygs
 * @date 2023/4/4 14:40
 */
@Configuration(proxyBeanMethods = false)
public class HibernateCacheAutoConfiguration {

//    @Bean
//    @Primary
////    @ConditionalOnProperty(value = "spring.jpa.properties.hibernate.cache.use_second_level_cache", havingValue = "true")
//    public HibernatePropertiesCustomizer setRegionFactory(RedissonClient redisson) {
//        return hibernateProperties -> {
//            hibernateProperties.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, true);
//            hibernateProperties.put(AvailableSettings.CACHE_REGION_FACTORY, new New2RedisRegionFactory(redisson));
//            hibernateProperties.put(AvailableSettings.CACHE_REGION_PREFIX, "xxxxxxxxxxx");
//            hibernateProperties.put(AvailableSettings.JPA_SHARED_CACHE_MODE, SharedCacheMode.ENABLE_SELECTIVE);
//            hibernateProperties.put(AvailableSettings.USE_QUERY_CACHE, true);
////            hibernateProperties.put("javax.persistence.sharedCache.mode", "ENABLE_SELECTIVE");
//        };
//    }

    @Bean
    public CustomHibernatePropertiesCustomizer hibernatePropertiesCustomizer(RedissonClient redisson) {
        return new CustomHibernatePropertiesCustomizer(redisson);
    }
}
