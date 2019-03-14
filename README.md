# ozonosfer

[![Build Status](https://travis-ci.org/iyzico/ozonosfer.svg?branch=master)](https://travis-ci.org/iyzico/ozonosfer)

Intelligent scalable rate limiter

# 1. Requirements

For ozonosfer-spring JDK 1.8 or newer is required.


# 2. Usage

```java
    @RateLimit(prefix = "app:method", key = "#request.authenticationId", windowSize = MINUTE, limit = 10)
    public void rateLimitedMethod(SampleRequest request) {
        System.out.println("rate limited method executed!");
    }
```

# 3. CONFIG

```
ozonosfer:
  toggling: white-list
```

### 3.1 Toggling
   Ozonosfer use redis sets for toggling rate limiting. The redis key is  ```ozon-list```. You can add keys to list with redis-cli ```SADD ozon-list "132"```. Default value is ```white-list```. 
   
   ```white-list``` ozonosfer only limits the keys in the list.
   
   ```black-list``` ozonosfer limits every request except key in the list. 

# 4. Options

* ```prefix``` - the prefix of identifier to limit against (retrieved from method parameter)
* ```key``` - the identifier to limit against (retrieved from method parameter)
* ```windowSize``` - the size of a window. Can be SECOND, MINUTE or HOUR.
* ```limit``` - maximum number of requests in the given window size.

Note: ```key``` expression supports fields inside nested objects ("#request.user.id")
