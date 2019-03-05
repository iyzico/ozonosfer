package com.iyzico.ozonosfer.FT;


import com.iyzico.ozonosfer.Application;
import com.iyzico.ozonosfer.domain.request.SampleRequest;
import com.netflix.hystrix.Hystrix;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalTime;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedisLateLimiterFT {

    private static final String OZONOSFER_ENABLED_LIST = "ozon-enabled-list";

    @LocalServerPort
    private int port;

    Jedis jedis = new Jedis();

    @Before
    public void setup() throws IOException, URISyntaxException, InterruptedException {
        jedis.configSet("timeout", "30");
        resetHystrix();
    }

    private void resetHystrix() {
        Hystrix.reset();
    }

    @Test
    public void should_return_rate_limit_exceeded_message_when_rate_limit_exceeded() throws JSONException {
        //given
        TestRestTemplate restTemplate = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();

        SampleRequest request = new SampleRequest();
        jedis.sadd(OZONOSFER_ENABLED_LIST, "15");
        request.setAuthenticationId("15");
        request.setMessage("Hello ozonosfer");

        HttpEntity<SampleRequest> entity = new HttpEntity<SampleRequest>(request, headers);

        //when
        ResponseEntity<String> response = null;
        for (int i = 0; i < 19; i++) {
            response = restTemplate.exchange(
                    createURLWithPort("/limitedService"),
                    HttpMethod.POST, entity, String.class);
        }

        //then
        String expected = "{\"message\":\"Rate limit exceeded\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    @Test
    public void should_return_message_and_assert_number_of_calls_is_one_when_first_call_of_rate_limiter() throws JSONException {
        //given
        SampleRequest request = new SampleRequest();
        jedis.sadd(OZONOSFER_ENABLED_LIST, "16");
        request.setAuthenticationId("16");
        request.setMessage("ozonosfer");
        LocalTime now = LocalTime.now();
        TestRestTemplate restTemplate = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SampleRequest> entity = new HttpEntity<SampleRequest>(request, headers);
        String expected = "{\"message\":\"ozonosfer\"}";

        //when
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/limitedService"),
                HttpMethod.POST, entity, String.class);

        //then
        String value = jedis.get("ozon:m:limitedService:getMessage:16:" + now.getMinute());
        assertThat(value).isEqualTo("1");
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    @Test
    public void should_return_message_and_assert_number_of_calls_is_five_when_fifth_call_of_rate_limiter() throws JSONException, IOException, InterruptedException {
        //given
        jedis.sadd(OZONOSFER_ENABLED_LIST, "17");
        SampleRequest request = new SampleRequest();
        request.setAuthenticationId("17");
        request.setMessage("ozonosfer");
        LocalTime now = LocalTime.now();
        TestRestTemplate restTemplate = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SampleRequest> entity = new HttpEntity<SampleRequest>(request, headers);
        String expected = "{\"message\":\"ozonosfer\"}";

        //when
        ResponseEntity<String> response = null;

        for (int i = 0; i < 5; i++) {
            response = restTemplate.exchange(
                    createURLWithPort("/limitedService"),
                    HttpMethod.POST, entity, String.class);

        }

        //then
        String value = jedis.get("ozon:m:limitedService:getMessage:17:" + now.getMinute());

        assertThat(value).isEqualTo("5");
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    @Test
    public void should_return_message_between_1000ms_and_1050ms_for_first_4_attempt_when_redis_has_2000ms_latency() throws JSONException, IOException, InterruptedException {
        //given
        jedis.sadd(OZONOSFER_ENABLED_LIST, "18");
        increaseRedisLatency(2000);

        SampleRequest request = new SampleRequest();
        request.setAuthenticationId("18");
        request.setMessage("ozonosfer");

        TestRestTemplate restTemplate = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SampleRequest> entity = new HttpEntity<SampleRequest>(request, headers);
        String expected = "{\"message\":\"ozonosfer\"}";
        ResponseEntity<String> response = null;

        //when
        restTemplate.exchange(
                createURLWithPort("/limitedService"),
                HttpMethod.POST, entity, String.class);

        //then
        for (int i = 0; i < 3; i++) {
            long startTime = System.nanoTime();
            response = restTemplate.exchange(
                    createURLWithPort("/limitedService"),
                    HttpMethod.POST, entity, String.class);
            long durationMS = (System.nanoTime() - startTime) / 1000000;
            assertThat(durationMS).isBetween(1000L, 1050L);
            JSONAssert.assertEquals(expected, response.getBody(), false);
        }
    }

    @Test
    public void should_return_message_under_50ms_after_fifth_attempt_when_redis_has_2000ms_latency() throws JSONException, IOException, InterruptedException {
        //given
        jedis.sadd(OZONOSFER_ENABLED_LIST, "19");
        increaseRedisLatency(2000);

        SampleRequest request = new SampleRequest();
        request.setAuthenticationId("19");
        request.setMessage("ozonosfer");

        TestRestTemplate restTemplate = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<SampleRequest> entity = new HttpEntity<SampleRequest>(request, headers);
        String expected = "{\"message\":\"ozonosfer\"}";
        ResponseEntity<String> response = null;

        //when
        restTemplate.exchange(
                createURLWithPort("/limitedService"),
                HttpMethod.POST, entity, String.class);
        for (int i = 0; i < 5; i++) {
            restTemplate.exchange(
                    createURLWithPort("/limitedService"),
                    HttpMethod.POST, entity, String.class);
        }
        //then
        for (int i = 0; i < 3; i++) {
            long startTime = System.nanoTime();
            response = restTemplate.exchange(
                    createURLWithPort("/limitedService"),
                    HttpMethod.POST, entity, String.class);
            long durationMS = (System.nanoTime() - startTime) / 1000000;
            assertThat(durationMS).isLessThan(50);
            JSONAssert.assertEquals(expected, response.getBody(), false);
        }

    }

    @After
    public void tearDown() throws InterruptedException, IOException {
        removeRedisLatency();
        jedis.flushAll();
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private void increaseRedisLatency(Integer miliSecond) throws InterruptedException, IOException {
        Process proc = Runtime.getRuntime().exec("docker exec ozonosfer-test_redis_master_1 tc qdisc add dev eth0 root netem delay " + Integer.toString(miliSecond) + "ms");
        proc.waitFor();
    }

    private void removeRedisLatency() throws IOException, InterruptedException {
        Process proc = Runtime.getRuntime().exec("docker exec ozonosfer-test_redis_master_1 tc qdisc del dev eth0 root");
        proc.waitFor();
    }
}
