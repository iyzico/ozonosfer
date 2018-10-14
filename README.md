# ozonosfer

[![Build Status](https://travis-ci.org/iyzico/iyzipay-java.svg?branch=master)](https://travis-ci.org/iyzico/ozonosfer)

Intelligent scalable rate limiter

# Requirements

For ozonosfer-spring JDK 1.8 or newer is required.


# Usage

```java
    @RateLimit(prefix = "app:method", key = "#request.authenticationId", windowSize = MINUTE, limit = 10)
    public void rateLimitedMethod(SampleRequest request) {
        System.out.println("rate limited method executed!");
    }
```

Note: ```key``` expression supports fields inside nested objects ("#request.user.id")

# Options

* ```prefix``` - the prefix of identifier to limit against (retrieved from method parameter)
* ```key``` - the identifier to limit against (retrieved from method parameter)
* ```windowSize``` - the size of a window. Can be SECOND, MINUTE or HOUR.
* ```seconds``` - the duration of limit in given window size