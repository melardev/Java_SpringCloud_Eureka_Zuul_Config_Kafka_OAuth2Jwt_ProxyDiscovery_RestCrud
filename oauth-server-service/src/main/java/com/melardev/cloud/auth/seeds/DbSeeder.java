package com.melardev.cloud.auth.seeds;


import com.github.javafaker.Faker;
import com.melardev.cloud.auth.entities.Role;
import com.melardev.cloud.auth.entities.User;
import com.melardev.cloud.auth.services.UserService;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Random;
import java.util.stream.LongStream;

@Service
public class DbSeeder implements CommandLineRunner {


    @Autowired
    DataSource dataSource;

    @Autowired
    private UserService usersService;

    @Autowired
    Environment environment;

    @Override
    public void run(String... args) {
        System.out.printf("[+] We are using the following database connection string : %s\n" +
                "Go ahead into http://localhost:" + environment.getProperty("server.port") + "/api/h2-console and paste that connection string,\nusername=user,password=password, to access" +
                "the h2 database console ;)", ((HikariDataSource) dataSource).getJdbcUrl());

        Faker faker = new Faker(new Random(System.currentTimeMillis()));

        long usersCount = this.usersService.count();
        long usersToSeed = 3;
        usersToSeed -= usersCount;
        LongStream.range(0, usersToSeed).forEach(i -> {
            usersService.createUser(faker.name().username(), "password");
        });

        // Create an admin
        User adminUser = new User("admin", "password");
        adminUser.setRoles(Collections.singleton(new Role("ROLE_ADMIN")));
        usersService.createUser(adminUser);
    }
}
