package com.project.render.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @GetMapping("/health")
    public String health(){
        return "OK";
    }

    @GetMapping("/")
    public String home() {
        return "App is running!";
    }

}
