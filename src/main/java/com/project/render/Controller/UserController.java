package com.project.render.Controller;

import com.project.render.Entity.User;
import com.project.render.Repository.UserRepository;
import com.project.render.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user){
        return userService.register(user);
    }

    @PostMapping("/login")
    public  ResponseEntity<?> login(@RequestBody User user){
        Boolean found = userService.login(user);
        if(found) return ResponseEntity.ok("found");
        return new ResponseEntity<>("not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/user")
    public List<User> getall(){
        return userService.getall();
    }

    @GetMapping("/health")
    public String health(){
        return "OK";
    }

    @GetMapping("/")
    public String home() {
        return "App is running!";
    }

}
