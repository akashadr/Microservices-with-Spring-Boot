package com.project1.project1.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.project1.project1.Model.AuthRequest;
import com.project1.project1.Model.User;
import com.project1.project1.Repository.UserRepository;
import com.project1.project1.service.JwtService;
import com.project1.project1.service.UserInfoService;
import com.project1.project1.service.KafkaMessagePublisher;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private KafkaMessagePublisher publisher;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @RequestMapping("/")
    @ResponseBody
    public String helloWorld() {
        return "Hello World!";
    }

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder encoder;

    // Save method is predefine method in Mongo Repository
    // with this method we will save user in our database
    @PostMapping("/addUser")
    public User addUser(@RequestBody User user) {
        publisher.sendMessageToTopic("Add User");
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    @PostMapping("/publish")
    public void sendEvents(@RequestBody User user) {
        publisher.sendEventsToTopic(user);
    }

    @GetMapping("/user/userProfile")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String userProfile() {
        return "Welcome to User Profile";
    }

    @GetMapping("/admin/adminProfile")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String adminProfile() {
        return "Welcome to Admin Profile";
    }

    // findAll method is predefine method in Mongo Repository
    // with this method we will all user that is save in our database
    @GetMapping("/getAllUser")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<User> getAllUser() {
        publisher.sendMessageToTopic("Get All User");
        return userRepo.findAll();
    }

    @GetMapping("/getUser/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        // Find the user by ID
        publisher.sendMessageToTopic("Get User");
        User user = userRepo.findById(id).orElse(null);

        if (user != null) {
            // If the user is found, return it with HTTP status 200 (OK)
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            // If the user is not found, return a message with HTTP status 404 (Not Found)
            return new ResponseEntity<>("User not found with ID: " + id, HttpStatus.NOT_FOUND);
        }
    }

    // Update a user by ID
    @PutMapping("/updateUser/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public User updateUser(@PathVariable String id, @RequestBody User user) {
        // Check if the user with the given ID exists
        publisher.sendMessageToTopic("Update User");
        User existingUser = userRepo.findById(id).orElse(null);

        if (existingUser != null) {
            // Update the existing user's properties
            existingUser.setName(user.getName());
            existingUser.setRollNumber(user.getRollNumber());
            existingUser.setEmail(user.getEmail());
            existingUser.setRoles(user.getRoles());
            // existingUser.setPassword(user.getPassword());

            // Save the updated user
            return userRepo.save(existingUser);
        } else {
            // Return null or throw an exception based on your requirements
            return null;
        }
    }

    // Delete a user by ID
    @DeleteMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable String id) {
        // Check if the user with the given ID exists
        publisher.sendMessageToTopic("Delete User");
        User existingUser = userRepo.findById(id).orElse(null);

        if (existingUser != null) {
            // Delete the user
            userRepo.deleteById(id);
            return "User with ID " + id + " has been deleted.";
        } else {
            // Return a message indicating that the user was not found
            return "User with ID " + id + " not found.";
        }
    }

    @PostMapping("/generateToken")
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        publisher.sendMessageToTopic("Generate Token");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(authRequest.getUsername());
        } else {
            throw new UsernameNotFoundException("invalid user request !");
        }
    }
}
