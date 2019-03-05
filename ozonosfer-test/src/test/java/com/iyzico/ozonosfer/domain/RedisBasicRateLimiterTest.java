package com.iyzico.ozonosfer.domain;

import com.iyzico.ozonosfer.IntegrationTest;
import com.iyzico.ozonosfer.domain.exception.RateLimitedException;
import com.iyzico.ozonosfer.domain.request.SampleRequest;
import com.iyzico.ozonosfer.domain.service.LimitedService;
import com.netflix.hystrix.Hystrix;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalTime;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RedisBasicRateLimiterTest extends IntegrationTest {

    @Autowired
    LimitedService myLimitedService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    private static final String OZON_ENABLED_LIST = "ozon-enabled-list";

    @Before
    public void setup() throws IOException, URISyntaxException {
        resetHystrix();
    }

    private void resetHystrix() {
        Hystrix.reset();
    }

    public RedisBasicRateLimiterTest() {
    }

    @Test
    public void should_return_one_when_first_call_of_rate_limiter_and_window_type_is_minute() {
        //given
        SampleRequest request = new SampleRequest();
        redisTemplate.opsForSet().add(OZON_ENABLED_LIST, "15");
        request.setAuthenticationId("15");
        LocalTime now = LocalTime.now();

        //when
        Integer result = myLimitedService.rateLimitedMethod(request);

        //then
        String value = redisTemplate.opsForValue().get("ozon:m:app:method:15:" + now.getMinute());

        assertThat(value).isEqualTo("1");
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void should_return_five_when_fifth_call_of_rate_limiter_and_window_type_is_minute() {
        //given
        redisTemplate.opsForSet().add(OZON_ENABLED_LIST, "16");
        SampleRequest request = new SampleRequest();
        request.setAuthenticationId("16");
        LocalTime now = LocalTime.now();

        //when
        for (int i = 0; i < 5; i++) {
            myLimitedService.rateLimitedMethod(request);
        }

        //then
        String value = redisTemplate.opsForValue().get("ozon:m:app:method:16:" + now.getMinute());

        assertThat(value).isEqualTo("5");
    }

    @Test
    public void should_throw_exception_when_rate_limit_exceeded() {
        //given
        SampleRequest request = new SampleRequest();
        redisTemplate.opsForSet().add(OZON_ENABLED_LIST, "17");
        request.setAuthenticationId("17");
        LocalTime now = LocalTime.now();

        //when
        for (int i = 0; i < 10; i++) {
            myLimitedService.rateLimitedMethod(request);
        }
        //when
        Throwable throwable = catchThrowable(() -> myLimitedService.rateLimitedMethod(request));

        //then
        Assertions.assertThat(throwable).isNotNull();
        Assertions.assertThat(throwable).isInstanceOf(RateLimitedException.class).hasMessageContaining("RateLimitRequest{prefix='app:method', key=17, limit=10, windowSize=MINUTE}");

        String value = redisTemplate.opsForValue().get("ozon:m:app:method:17:" + now.getMinute());

        assertThat(value).isEqualTo("10");
    }

    @After
    public void tearDown() throws InterruptedException {
        redisTemplate.delete(redisTemplate.keys("*"));
    }
}
