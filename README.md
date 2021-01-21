# Helidon CloudEvents demo
Helidon messaging with CloudEvents and Kafka connector.

Setup OCI Stream as described in the medium article [Helidon messaging with Oracle Streaming Service](https://medium.com/helidon/helidon-messaging-with-oracle-streaming-service-68e0ef423853) and use [CloudEvents Kafka (de)serializers](https://cloudevents.github.io/sdk-java/kafka) for communication (see [application.yaml](src/main/resources/application.yaml)).

1. Build and run:

```shell
mvnd package && \
java -DOCI_AUTH_TOKEN="YOUR AUTH TOKEN" -jar ./target/helidon-cloudevents-demo.jar
```

2. Open in a browser:
http://localhost:7001
   
3. Send messages and wait for async response(Frank the Helidon bird flying over the screen).

