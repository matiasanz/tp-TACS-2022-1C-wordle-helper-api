package com.tacs2022.wordlehelper.service;

import java.util.List;

import com.tacs2022.wordlehelper.domain.user.Result;
import com.tacs2022.wordlehelper.domain.user.User;
import com.tacs2022.wordlehelper.repos.UserRepository;

import com.tacs2022.wordlehelper.service.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    UserRepository userRepo;

    public List<User> findAll() {
        return (List<User>) userRepo.findAll();
    }

    public User findById(Long id) {
        return userRepo.findById(id).orElseThrow(
            () -> new NotFoundException("User with Id: " + id + " was not found")
        );
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepo.findById(id).orElseThrow(
                () -> new NotFoundException("User with Id: " + id + " was not found")
        );
        userRepo.delete(user);
    }

    @Transactional
    public User save(User newUser){
        userRepo.save(newUser);
        return newUser;
    }

    @Transactional
    public void update(User existingUser){
        userRepo.save(existingUser);
    }

    @Transactional
    public void addResult(Long userId, Result result){
        //TODO
    }
}
