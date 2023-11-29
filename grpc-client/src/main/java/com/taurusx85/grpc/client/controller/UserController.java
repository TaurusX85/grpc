package com.taurusx85.grpc.client.controller;


import com.taurusx85.grpc.client.dto.input.NotificationInput;
import com.taurusx85.grpc.client.dto.input.UserCreationInput;
import com.taurusx85.grpc.client.dto.output.UserDTO;
import com.taurusx85.grpc.client.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("user")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }


    /**
     * Unary synchronous call
     * In case of error - send back a status explicitly
     */
    @PostMapping
    public ResponseEntity<Integer> create(@RequestBody UserCreationInput input) {
        Integer id = userService.create(input.getName());
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }


    /**
     * Unary synchronous call
     */
    @GetMapping("{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Integer id) {
        UserDTO user = userService.getById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    /**
     * Unary asynchronous call
     */
    @PostMapping("{id}/notify")
    public ResponseEntity notifyUser(@PathVariable Integer id, @RequestBody NotificationInput input) {
        userService.notifyUser(id, input);
        log.info("Notifications sent");
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * Bidirectional streaming
     */
    @PostMapping("multiple")
    public ResponseEntity<List<Integer>> createMultiple(@RequestBody List<UserCreationInput> input) {
        List<Integer> ids = userService.createMultiple(input);
        log.info("Created users: " + ids);
        return new ResponseEntity<>(ids, HttpStatus.CREATED);
    }


    /**
     * Unidirectional server-side streaming
     */
    @GetMapping("all")
    public ResponseEntity<List<UserDTO>> getAll() {
        log.info("All users requested");
        List<UserDTO> all = userService.getAll();
        log.info("All users received");
        return new ResponseEntity<>(all, HttpStatus.OK);
    }


    /**
     * Unidirectional client-side streaming
     */
    @DeleteMapping("multiple")
    public ResponseEntity deleteMultiple(@RequestParam List<Integer> ids) {
        log.info("Going to remove multiple users");
        userService.deleteMultiple(ids);
        log.info("Users removed");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("deadline")
    public ResponseEntity<UserDTO> getByNameWithDeadline() {
        return new ResponseEntity<>(userService.getByNameWithDeadline(), HttpStatus.OK);
    }

    @GetMapping("cancel")
    public ResponseEntity<UserDTO> getByNameAndCancel() {
        return new ResponseEntity<>(userService.getByNameAndCancel(), HttpStatus.OK);
    }

}
