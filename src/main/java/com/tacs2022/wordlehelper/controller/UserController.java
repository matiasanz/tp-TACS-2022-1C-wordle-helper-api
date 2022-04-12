package com.tacs2022.wordlehelper.controller;

import java.util.ArrayList;
import java.util.Map;

import com.tacs2022.wordlehelper.domain.user.User;
import com.tacs2022.wordlehelper.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;

    // users endpoints

    @GetMapping()
    public ResponseEntity<ArrayList<User>> getAllUsers() {
        ArrayList<User> users = userService.findAll();

        return new ResponseEntity(users, HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> create(@RequestBody User newUser){
        User user = userService.save(newUser);
        return new ResponseEntity(user, HttpStatus.CREATED);
    }

    // users/{id} endpoints

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable(value = "id") Long id) {
        User user = userService.findById(id);
        return new ResponseEntity(user, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody User existingUser) {
        userService.update(existingUser);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity delete(@PathVariable(value = "id") Long id) {
        userService.delete(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
    
    @PostMapping("/users/{idUser}/results")
    public ResponseEntity<Object> postTournament(@RequestBody Map<String, Object> json, @PathVariable(value = "idUser") Long idTournament){
        return ResponseEntity.noContent().build();
    }
    
    
}
