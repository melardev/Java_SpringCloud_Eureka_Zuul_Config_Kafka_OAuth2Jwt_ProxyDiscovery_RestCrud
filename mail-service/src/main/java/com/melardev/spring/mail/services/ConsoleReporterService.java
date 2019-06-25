package com.melardev.spring.mail.services;

import com.melardev.spring.mail.models.Todo;
import org.springframework.stereotype.Service;

@Service("console")
public class ConsoleReporterService implements IReporterService {

    @Override
    public void report(Todo todo) {
        System.out.printf("Todo created; Title=%s; Description=%s, CreatedAt=%s, UpdatedAt=%s",
                todo.getTitle(), todo.getDescription(), todo.getCreatedAt(), todo.getUpdatedAt());
    }
}
