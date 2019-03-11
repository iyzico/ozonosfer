package com.iyzico.ozonosfer.domain;

import com.iyzico.ozonosfer.IntegrationTest;
import com.iyzico.ozonosfer.config.EmbeddedRedis;
import com.iyzico.ozonosfer.domain.model.RateLimitRequest;
import com.iyzico.ozonosfer.domain.model.RateLimitWindowSize;
import com.iyzico.ozonosfer.domain.request.SampleRequest;
import com.iyzico.ozonosfer.domain.service.LimitedService;
import com.iyzico.ozonosfer.domain.service.RateLimiterService;
import com.iyzico.ozonosfer.infrastructure.service.RedisBasicRateLimiterService;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Java6Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RedisBasicRateLimiterHystrixIT extends IntegrationTest {

    @Autowired
    LimitedService myLimitedService;

    @Autowired
    RateLimiterService rateLimiterService;

    @Autowired
    EmbeddedRedis embeddedRedis;

    @Before
    public void setup() throws IOException, URISyntaxException, InterruptedException {
        // don't start redis server on setup
        resetHystrix();
        warmUpCircuitBreaker();
        openCircuitBreakerAfterOneFailingRequest();
    }

    private void resetHystrix() {
        Hystrix.reset();
    }

    private void warmUpCircuitBreaker() {
        RateLimitRequest request = new RateLimitRequest();
        request.setKey("#request.authenticationId");
        request.setLimit(10);
        request.setPrefix("app:method");
        request.setWindowSize(RateLimitWindowSize.MINUTE);
        rateLimiterService.rateLimit(request);
    }

    @Test
    public void should_open_circuit_when_redis_server_is_down() throws InterruptedException {
        //given
        RateLimitRequest request = new RateLimitRequest();
        request.setKey("#request.authenticationId");
        request.setLimit(10);
        request.setPrefix("app:method");
        request.setWindowSize(RateLimitWindowSize.MINUTE);

        HystrixCircuitBreaker circuitBreaker = getCircuitBreaker();

        // demonstrates circuit is actually closed
        assertThat(circuitBreaker.isOpen()).isFalse();
        assertThat(circuitBreaker.allowRequest()).isTrue();

        // when redis is down
        embeddedRedis.stopRedis();
        rateLimiterService.rateLimit(request);

        // then circuit is open
        waitUntilCircuitBreakerOpens();
        assertThat(circuitBreaker.isOpen()).isTrue();
        assertThat(circuitBreaker.allowRequest()).isFalse();
    }

    @Test
    public void should_return_one_when_redis_is_down() throws InterruptedException {
        //given
        SampleRequest request = new SampleRequest();
        request.setAuthenticationId("15");

        HystrixCircuitBreaker circuitBreaker = getCircuitBreaker();

        // demonstrates circuit is actually closed
        assertThat(circuitBreaker.isOpen()).isFalse();
        assertThat(circuitBreaker.allowRequest()).isTrue();

        // when redis is down
        embeddedRedis.stopRedis();
        Integer result = myLimitedService.rateLimitedMethod(request);

        // then circuit is open and rate limited service executed!
        waitUntilCircuitBreakerOpens();
        assertThat(result).isEqualTo(1);
        assertThat(circuitBreaker.isOpen()).isTrue();
        assertThat(circuitBreaker.allowRequest()).isFalse();
    }

    private void waitUntilCircuitBreakerOpens() throws InterruptedException {
        /* one second is almost sufficient
           borrowed from https://github.com/Netflix/Hystrix/blob/v1.5.5/hystrix-core/src/test/java/com/netflix/hystrix/HystrixCircuitBreakerTest.java#L140
         */
        Thread.sleep(1000);
    }


    public static HystrixCircuitBreaker getCircuitBreaker() {
        return HystrixCircuitBreaker.Factory.getInstance(getCommandKey());
    }

    private static HystrixCommandKey getCommandKey() {
        return HystrixCommandKey.Factory.asKey(RedisBasicRateLimiterService.HYSTRIX_COMMAND_KEY);
    }

    private void openCircuitBreakerAfterOneFailingRequest() {
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command." + RedisBasicRateLimiterService.HYSTRIX_COMMAND_KEY + ".circuitBreaker.requestVolumeThreshold", 1);
    }
}
