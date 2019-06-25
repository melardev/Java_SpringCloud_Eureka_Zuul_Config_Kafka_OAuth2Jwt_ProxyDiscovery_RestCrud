package com.melardev.spring.mail.config;


import com.melardev.spring.mail.models.Todo;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;


    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        // What to do when there is not an initial offset or the offset does not exist
        // anymore for example when the data is deleted, earliest will reset the offset to the earliest
        // offset; other values: latest, none, anything else
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return properties;
    }

    @Bean
    public ConsumerFactory<String, Todo> consumerFactory() {
        JsonDeserializer<Todo> deserializer = new JsonDeserializer<>(Todo.class);
        deserializer.addTrustedPackages("com.melardev.cloud.rest.entities");

        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("com.melardev.cloud.rest.entities.Todo", Todo.class);
        ((DefaultJackson2JavaTypeMapper) deserializer.getTypeMapper()).setIdClassMapping(mappings);
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Todo> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Todo> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        return factory;
    }

    /*
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        // Replace with your email and password
        mailSender.setUsername("non-existent-email-microservices@gmail.com");
        mailSender.setPassword("very_very_fake_password_remember_its_fake_");
        // Don't forget to enable less secure apps:
        // https://support.google.com/accounts/answer/6010255
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
    */
}
