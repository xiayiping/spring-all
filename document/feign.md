feign different loadbalancer map to different config:

https://github.com/KurdTt/feign_client_ssl_example/blob/master/client-feign/src/main/java/com/example/feign/client/common/DefaultLoadBalancerConfiguration.java


I got trustAnchor empty problem, probably because I didn't set

```shell
java -Djavax.net.ssl.trustStore=/some/loc/on/server/ our_truststore.jks -Djavax.net.ssl.trustStorePassword=our_password -jar application.jar

```

https://www.baeldung.com/java-trustanchors-parameter-must-be-non-empty

The client for LoadBalancer is a ```FeignBlockingLoadBalancerClient```, 
which wraps a normal HttpClient, here I'll use ApacheHttp5Client