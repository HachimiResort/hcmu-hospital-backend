package org.hcmu.hcmuserver.config;


import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.properties.AliOssProperties;
import org.hcmu.hcmucommon.utils.AliOssUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用于创建AliOssUtil对象
 */
@Configuration
@EnableConfigurationProperties(AliOssProperties.class)
@Slf4j
public class OssConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties) {
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }
}
