package com.project.management.repository;

import com.project.management.Models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;



import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    @Query(value = "{ 'username': { $in: ?0 } }", fields = "{ '_id': 1 }")
    List<User> findAllUserIdByUsername(List<String> usernames);
    List<User> findAllById(List<String> userIds);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}

