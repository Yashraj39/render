package com.project.render.Service;

import com.project.render.Entity.User;
import com.project.render.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<User> register(User user){
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    public Boolean login(User user){
        Optional<User> found = userRepository.findByEmailAndPassword(user.getEmail(), user.getPassword());
        if(found.isPresent()) return true;
        return false;
    }

    @GetMapping("/user")
    public List<User> getall(){
        return userRepository.findAll();
    }

}
