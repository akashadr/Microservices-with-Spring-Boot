package com.project1.project1.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project1.project1.Model.User;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByName(String username);

}
