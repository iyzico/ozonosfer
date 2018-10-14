package com.iyzico.ozonosfer.infrastructure.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(RedisConfiguration.class)
@ComponentScan(basePackages = {"com.iyzico.ozonosfer"})
public class OzonosferConfiguration {
}
