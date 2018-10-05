package com.iyzico.ozonosfer.domain;

public class RateLimitRequest {

    private Object key;
    private long limit;
    private long seconds;

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    @Override
    public String toString() {
        return "RateLimitRequest{key=" + key + ", limit=" + limit + ", seconds=" + seconds + '}';
    }
}