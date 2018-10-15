package com.iyzico.ozonosfer;

import com.iyzico.ozonosfer.infrastructure.configuration.OzonosferConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Import;

@EnableHystrix
@SpringBootApplication
@Import(OzonosferConfiguration.class)
public class Application {
}
