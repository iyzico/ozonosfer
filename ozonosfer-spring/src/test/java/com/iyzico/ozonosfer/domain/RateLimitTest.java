package com.iyzico.ozonosfer.domain;

import com.iyzico.ozonosfer.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class RateLimitTest {

    @Autowired
    RateLimitedService rateLimitedService;

    public RateLimitTest() {
    }

    @Test
    public void shouldLimit() {
        //given
        SampleRequest request = new SampleRequest();
        rateLimitedService.rateLimitedMethod(request);
    }
}
