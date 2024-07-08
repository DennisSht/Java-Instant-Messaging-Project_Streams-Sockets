
### URL
* https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html
* https://github.com/provectus/kafka-ui
* https://hub.docker.com/r/bitnami/kafka

* https://docs.confluent.io/kafka-clients/java/current/overview.html

### Docker

```bash
docker run -d --name kafka-ui --hostname kafka-ui -p 8080:8080 -e DYNAMIC_CONFIG_ENABLED=true provectuslabs/kafka-ui
```
KafkaUI: http://localhost:8080


```bash
docker run -d --name kafka-server --hostname kafka-server \
    -p 9092:9092 \
    -e KAFKA_CFG_NODE_ID=0 \
    -e KAFKA_CFG_PROCESS_ROLES=controller,broker \
    -e KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true \
    -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
    -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
    -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka-server:9093 \
    -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
    bitnami/kafka:latest
```

