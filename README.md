# ozonosfer

Intelligent scalable rate limiter

# Requirements

For ozonosfer-spring JDK 1.8 or newer is required.


# Usage

```java
    @RateLimit(prefix = "app:method", key = "#request.authenticationId", windowType = MINUTE, limit = 10)
    public void rateLimitedMethod(SampleRequest request) {
        System.out.println("rate limited method executed!");
    }
```

Note: ```key``` expression supports fields inside nested objects ("#request.user.id")

# Options

* ```key``` - the identifier to limit against (retrieved from method parameter)
* ```limit``` - max requests within seconds
* ```seconds``` - duration of limit in seconds