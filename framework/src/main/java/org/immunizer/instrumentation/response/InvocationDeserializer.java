package org.immunizer.instrumentation.response;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.gson.Gson;

import org.immunizer.instrumentation.Invocation;

public class InvocationDeserializer implements Deserializer<Invocation> {

    private Gson gson = new Gson();

    public InvocationDeserializer() {}
    
    @Override
    public Invocation deserialize(String topic, byte[] bytes) {
        return gson.fromJson(new String(bytes), Invocation.class);
    }
}