package org.immunizer.instrumentation.monitoring;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class InvocationProducer {

    private static InvocationProducer singleton;
    private KafkaProducer<String, Invocation> producer;
    private static final String BOOTSTRAP_SERVERS = "kafka:9092";
    private static final String TOPIC = "Invocations";

    private InvocationProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.immunizer.instrumentation.monitoring.InvocationSerializer");
        producer = new KafkaProducer<String, Invocation>(props);
    }

    public static InvocationProducer getSingleton() {
        if (singleton == null) {
            singleton = new InvocationProducer();
        }
        return singleton;
    }

    public void send(Invocation invocation) {
        try{
            producer.send(new ProducerRecord<String, Invocation>(TOPIC, invocation));
        } catch(Throwable th) {
        }
    }
}