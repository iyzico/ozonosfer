package com.iyzico.ozonosfer.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RateLimitTest {

    public RateLimitTest() {
    }

    @Test
    public void shouldLimit() {
        //given
        SampleRequest request = new SampleRequest();
        this.rateLimitedMethod(request);
    }

    @RateLimit(key = "request.authenticationId", seconds = 60, limit = 1000)
    public void rateLimitedMethod(SampleRequest request) {
        System.out.println("rate limited method executed!");
    }
}
