
## Features

- "caller" call rest api of "echo" using http/https/mTLS connections. (done)
- spring boot mvc test  
  - end to end
  - using mock mvc
  - end to end also mock other microservice's response
- config open-telemetry:  (TBD)
  - span
  - metrics 
    - otel, prometheus, grafana
    - add app_name as tag so grafana can distinguish different application
  - log