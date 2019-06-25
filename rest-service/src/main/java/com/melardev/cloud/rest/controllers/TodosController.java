package com.melardev.cloud.rest.controllers;

import com.melardev.cloud.rest.dtos.responses.ErrorResponse;
import com.melardev.cloud.rest.entities.Todo;
import com.melardev.cloud.rest.messaging.KafkaSender;
import com.melardev.cloud.rest.repositories.TodosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class TodosController {

    @Autowired
    private TodosRepository todosRepository;

    @Autowired
    KafkaSender kafkaSender;

    @GetMapping
    public Iterable<Todo> index() {
        return this.todosRepository.findAllHqlSummary();
    }

    @GetMapping("/{id}")
    public ResponseEntity get(@PathVariable("id") Long id) {
        Optional<Todo> todo = this.todosRepository.findById(id);
        /*
                if (todo.isPresent())
            return new ResponseEntity<>(todo.get(), HttpStatus.OK);
        else
            return new ResponseEntity<>(new Todo(), HttpStatus.NOT_FOUND);
         */
        return todo.map(todo1 -> new ResponseEntity(todo1, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity(new ErrorResponse("Not found"), HttpStatus.NOT_FOUND));
    }

    @GetMapping("/pending")
    public List<Todo> getNotCompletedTodos() {
        return this.todosRepository.findByHqlCompletedIs(false);
    }

    @GetMapping("/completed")
    public List<Todo> getCompletedTodos() {
        return todosRepository.findByHqlCompletedIs(true);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER') and #oauth2.hasScope('write')")
    public ResponseEntity<Todo> create(@Valid @RequestBody Todo todo) {
        Todo savedTodo = todosRepository.save(todo);
        kafkaSender.send(savedTodo);
        return new ResponseEntity<>(savedTodo, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') and #oauth2.hasScope('write')")
    public ResponseEntity update(@PathVariable("id") Long id,
                                 @RequestBody Todo todoInput) {
        Optional<Todo> optionalTodo = todosRepository.findById(id);
        if (optionalTodo.isPresent()) {
            Todo todo = optionalTodo.get();
            todo.setTitle(todoInput.getTitle());

            String description = todoInput.getDescription();
            if (description != null)
                todo.setDescription(description);

            todo.setCompleted(todoInput.isCompleted());
            return ResponseEntity.ok(todosRepository.save(optionalTodo.get()));
        } else {
            return new ResponseEntity<>(new ErrorResponse("This todo does not exist"), HttpStatus.NOT_FOUND);
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') and #oauth2.hasScope('write')")
    public ResponseEntity delete(@PathVariable("id") Long id) {
        Optional<Todo> todo = todosRepository.findById(id);
        if (todo.isPresent()) {
            todosRepository.delete(todo.get());
            return ResponseEntity.noContent().build();
        } else {
            return new ResponseEntity<>(new ErrorResponse("This todo does not exist"), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') and #oauth2.hasScope('write')")
    public ResponseEntity deleteAll() {
        todosRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/after/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Todo> getByDateAfter(@PathVariable("date") @DateTimeFormat(pattern = "dd-MM-yyyy") Date date) {
        Iterable<Todo> articlesIterable = todosRepository.findByCreatedAtAfter(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        List<Todo> articleList = new ArrayList<>();
        articlesIterable.forEach(articleList::add);
        return articleList;
    }

    @GetMapping(value = "/before/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Todo> getByDateBefore(@PathVariable("date") @DateTimeFormat(pattern = "dd-MM-yyyy") Date date) {
        Iterable<Todo> articlesIterable = todosRepository.findByCreatedAtBefore(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        List<Todo> articleList = new ArrayList<>();
        articlesIterable.forEach(articleList::add);
        return articleList;
    }

}
