package Controller;

import com.project.render.Entity.User;
import com.project.render.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserRepository repo;

    @GetMapping("/users")
    public List<User> getall(){
        return repo.findAll();
    }

    @PostMapping("/users")
    public User add(@RequestBody User user){
        return repo.save(user);
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
