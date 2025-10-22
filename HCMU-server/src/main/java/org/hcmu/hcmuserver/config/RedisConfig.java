package org.hcmu.hcmuserver.config;

import org.hcmu.hcmucommon.annotation.RequestKeyParam;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.time.Duration;

/**
 * Redis配置类
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJsonRedisSerializer serializer = new FastJsonRedisSerializer(Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    //    redis缓存管理器
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)) // 设置缓存有效期10min
                .disableCachingNullValues() // 不缓存空值
                // TODO: 设置缓存前缀
                .prefixCacheNameWith("cache:")
                // 设置 key 序列化
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 设置 value 序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new FastJsonRedisSerializer(Object.class)));

        return RedisCacheManager.builder(factory).cacheDefaults(config).transactionAware().build();
    }

    //自定义请求key的生成策略
    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            //获取Method上的所有参数
            final Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                //获取参数的注解
                final RequestKeyParam keyParam = parameters[i].getAnnotation(RequestKeyParam.class);
                //如果属性不是RequestKeyParam注解，则不处理
                if (keyParam == null) {
                    continue;
                }
                //如果属性是RequestKeyParam注解，则拼接 连接符 "& + RequestKeyParam"
                sb.append(":").append(params[i]);
            }
            //如果方法上没有加RequestKeyParam注解
            if (!StringUtils.hasText(sb.toString())) {
                //获取方法上的多个注解（为什么是两层数组：因为第二层数组是只有一个元素的数组）
                final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                //循环注解
                for (int i = 0; i < parameterAnnotations.length; i++) {
                    final Object object = params[i];
                    //获取注解类中所有的属性字段
                    final Field[] fields = object.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        //判断字段上是否有RequestKeyParam注解
                        final RequestKeyParam annotation = field.getAnnotation(RequestKeyParam.class);
                        //如果没有，跳过
                        if (annotation == null) {
                            continue;
                        }
                        //如果有，设置Accessible为true（为true时可以使用反射访问私有变量，否则不能访问私有变量）
                        field.setAccessible(true);
                        //如果属性是RequestKeyParam注解，且不为空，则拼接连接符" : + RequestKeyParam"
                        Object value = ReflectionUtils.getField(field, object);
                        if (value != null) sb.append(":").append(value);
                    }
                }
            }
            return sb.length() != 0 ? sb.substring(1, sb.length()) : "";
        };
    }


}
