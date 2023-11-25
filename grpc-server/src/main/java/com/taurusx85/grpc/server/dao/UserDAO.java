package com.taurusx85.grpc.server.dao;

import com.taurusx85.grpc.server.entity.User;
import com.taurusx85.grpc.server.exception.AlreadyExistsException;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 *  Simulate DB access
 */
@Repository
public class UserDAO {

    private final List<User> users = new ArrayList<>();

    public Optional<User> getById(int id) {
        return users.stream()
                    .filter(user -> user.getId() == id)
                    .findFirst();
    }

    public Collection<User> getAll() {
        return users;
    }

    public int create(String name) {
        if (existsByName(name))
            throw new AlreadyExistsException("User with name: " + name + " already exists");

        User user = new User();
        user.setId(new Random().nextInt(Integer.MAX_VALUE));
        user.setName(name);
        save(user);
        return user.getId();
    }


    //    ====================== PRIVATE ==================

    private boolean existsByName(String name) {
        return users.stream().anyMatch(user -> user.getName().equals(name));
    }

    private void save(User user) {
        users.add(user);
    }

    public boolean removeById(int id) {
        return users.removeIf(user -> user.getId() == id);
    }
}
