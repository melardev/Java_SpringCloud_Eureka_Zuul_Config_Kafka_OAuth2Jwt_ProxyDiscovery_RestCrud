package com.melardev.cloud.rest.messaging;

import com.melardev.cloud.rest.config.KafkaConfig;
import com.melardev.cloud.rest.entities.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaSender {

    @Autowired
    private KafkaTemplate<String, Todo> kafkaTemplate;

    @Autowired
    KafkaConfig kafkaConfig;

    public void send(Todo todo) {
        kafkaTemplate.send(kafkaConfig.getTodoCreatedTopic(), todo);
    }


}
