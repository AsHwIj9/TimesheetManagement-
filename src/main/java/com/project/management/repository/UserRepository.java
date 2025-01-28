package com.project.management.repository;

import com.project.management.Models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;



import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    List<User> findAllById(List<String> userIds);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
