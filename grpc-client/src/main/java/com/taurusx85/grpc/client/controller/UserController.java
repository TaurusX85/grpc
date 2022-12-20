package com.taurusx85.grpc.client.controller;


import com.taurusx85.grpc.client.dto.input.NotificationInput;
import com.taurusx85.grpc.client.dto.input.UserCreationInput;
import com.taurusx85.grpc.client.dto.output.UserDTO;
import com.taurusx85.grpc.client.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping
    public ResponseEntity<Integer> create(@RequestBody UserCreationInput input) {
        Integer id = userService.create(input.getName());
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Integer id) {
        UserDTO user = userService.getById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("{id}/notify")
    public ResponseEntity notifyUser(@PathVariable Integer id, @RequestBody NotificationInput input) {
        userService.notifyUser(id, input);
        return new ResponseEntity(HttpStatus.OK);
    }
}
