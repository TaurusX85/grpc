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
     * unidirectional synchronous
     */
    @PostMapping
    public ResponseEntity<Integer> create(@RequestBody UserCreationInput input) {
        Integer id = userService.create(input.getName());
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }


    /**
     * unidirectional synchronous
     */
    @GetMapping("{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Integer id) {
        UserDTO user = userService.getById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    /**
     * unidirectional asynchronous
     */
    @PostMapping("{id}/notify")
    public ResponseEntity notifyUser(@PathVariable Integer id, @RequestBody NotificationInput input) {
        userService.notifyUser(id, input);
        return new ResponseEntity(HttpStatus.OK);
    }


    /**
     * bidirectional streaming
     */
    @PostMapping("multiple")
    public ResponseEntity<List<Integer>> createMultiple(@RequestBody List<UserCreationInput> input) {
        List<Integer> ids = userService.createMultiple(input);
        log.info("Created users: " + ids);
        return new ResponseEntity<>(ids, HttpStatus.CREATED);
    }


    /**
     * unidirectional server-side streaming
     */
    @GetMapping("all")
    public ResponseEntity<List<UserDTO>> getAll() {
        return new ResponseEntity<>(userService.getAll(), HttpStatus.OK);
    }


    /**
     * unidirectional client-side streaming
     */
    @DeleteMapping("multiple")
    public ResponseEntity deleteMultiple(@RequestParam List<Integer> ids) {
        userService.deleteMultiple(ids);
        return new ResponseEntity(HttpStatus.OK);
    }

}
