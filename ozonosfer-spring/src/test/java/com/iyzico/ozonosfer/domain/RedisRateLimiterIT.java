package com.iyzico.ozonosfer.domain;

import com.iyzico.ozonosfer.domain.exception.RateLimitedException;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalTime;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class RedisRateLimiterIT extends IntegrationTest {

    @Autowired
    RateLimitedService rateLimitedService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    public RedisRateLimiterIT() {
    }

    @Test
    public void should_return_one_when_first_call_of_rate_limiter_and_window_type_is_minute() {
        //given
        SampleRequest request = new SampleRequest();
        request.setAuthenticationId("15");
        LocalTime now = LocalTime.now();

        //when
        rateLimitedService.rateLimitedMethod(request);

        //then
        String value = redisTemplate.opsForValue().get("ozon:m:app:method:15:" + now.getMinute());

        assertThat(value).isEqualTo("1");
    }

    @Test
    public void should_return_five_when_fifth_call_of_rate_limiter_and_window_type_is_minute() {
        //given
        SampleRequest request = new SampleRequest();
        request.setAuthenticationId("16");
        LocalTime now = LocalTime.now();

        //when
        for (int i = 0; i < 5; i++) {
            rateLimitedService.rateLimitedMethod(request);
        }

        //then
        String value = redisTemplate.opsForValue().get("ozon:m:app:method:16:" + now.getMinute());

        assertThat(value).isEqualTo("5");
    }

    @Test
    public void should_throw_exception_when_rate_limit_exceeded() {
        //given
        SampleRequest request = new SampleRequest();
        request.setAuthenticationId("17");
        LocalTime now = LocalTime.now();

        //when
        for (int i = 0; i < 10; i++) {
            rateLimitedService.rateLimitedMethod(request);
        }
        //when
        Throwable throwable = catchThrowable(() -> rateLimitedService.rateLimitedMethod(request));

        //then
        Assertions.assertThat(throwable).isNotNull();
        Assertions.assertThat(throwable).isInstanceOf(RateLimitedException.class).hasMessageContaining("RateLimitRequest{prefix='app:method', key=17, limit=10, windowType=MINUTE}");

        String value = redisTemplate.opsForValue().get("ozon:m:app:method:17:" + now.getMinute());

        assertThat(value).isEqualTo("10");
    }

    @After
    public void tearDown() {
        redisTemplate.delete(redisTemplate.keys("*"));
    }
}
