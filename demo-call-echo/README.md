## Features

- "caller" call rest api of "echo" using http/https/mTLS connections. (done)
- Spring boot mvc test  
  - reference: 
    - https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
    - https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing.spring-boot-applications.spring-mvc-tests
  - using end to end
  - using mock mvc
  - mock mvc also mock other microservice's response
- Enable system observation via open-telemetry: (almost done)
  - reference: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.observability
  - Tracing (TBD)
  - Metrics 
    - otel, prometheus, grafana
    - Add app_name as tag so grafana can distinguish different application
    - In "echo" the spring-boot-starter-data-jpa already depends on HikariCP which has already created metrics for you.
    - Add dynamic tag via MeterFilter
    - Add dynamic tag via MeterRegistryCustomizer
    - Use Timer and @Timer to add additional timer.
  - Log (TBD)

## Tricky things:

```shell
## if you got 
net stop hns

```
