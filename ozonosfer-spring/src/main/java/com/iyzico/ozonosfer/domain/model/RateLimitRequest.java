package com.iyzico.ozonosfer.domain.model;

public class RateLimitRequest {

    private String prefix;
    private Object key;
    private long limit;
    private RateLimitWindowSize windowSize;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

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

    public RateLimitWindowSize getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(RateLimitWindowSize windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public String toString() {
        return "RateLimitRequest{" +
                "prefix='" + prefix + '\'' +
                ", key=" + key +
                ", limit=" + limit +
                ", windowSize=" + windowSize +
                '}';
    }
}