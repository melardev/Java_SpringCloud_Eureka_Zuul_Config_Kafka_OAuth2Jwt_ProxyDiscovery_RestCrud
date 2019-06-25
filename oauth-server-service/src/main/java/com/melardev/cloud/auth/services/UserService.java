package com.melardev.cloud.auth.services;

import com.melardev.cloud.auth.entities.Role;
import com.melardev.cloud.auth.entities.User;
import com.melardev.cloud.auth.repositories.RoleRepository;
import com.melardev.cloud.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<User> account = userRepository.findByUsername(s);
        if (account.isPresent()) {
            return account.get();
        } else {
            throw new UsernameNotFoundException("Username not found!");
        }
    }

    public long count() {
        return userRepository.count();
    }

    public User createUser(String username, String password) {
        return createUser(new User(username, password));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRoles() == null || user.getRoles().size() == 0) {
            Optional<Role> optionUserRole = roleRepository.findByNameHql("ROLE_USER");
            Role userRole = optionUserRole.orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
            assert userRole.getId() != null;
            user.setRoles(Collections.singleton(userRole));
        } else {
            Set<Role> roles = user.getRoles();
            Set<Role> persistedRoles = new HashSet<>(roles.size());
            for (Role role : roles) {
                if (role.getId() == null) {
                    Role savedRole = roleRepository.save(role);
                    assert savedRole.getId() != null;
                    persistedRoles.add(savedRole);
                } else {
                    persistedRoles.add(role);
                }
            }

            user.setRoles(persistedRoles);
        }

        return userRepository.save(user);

    }
}
