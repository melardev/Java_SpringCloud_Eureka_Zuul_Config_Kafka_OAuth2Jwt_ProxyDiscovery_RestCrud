package com.melardev.spring.mail.messaging;

import com.melardev.spring.mail.models.Todo;
import com.melardev.spring.mail.services.IReporterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class KafkaReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaReceiver.class);

    @Autowired
    @Qualifier("console")
    private IReporterService reporterService;

    @Autowired
    public JavaMailSender emailSender;

    @KafkaListener(topics = "${app.kafka.topics.todo-created}")
    public void onTodoCreated(Todo todo) {
        LOGGER.info("Received Todo");
        reporterService.report(todo);
    }
}
