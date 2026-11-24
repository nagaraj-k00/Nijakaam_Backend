package com.nijakaam.repository;

import com.nijakaam.entity.EmployerProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EmployerProfileRepository extends MongoRepository<EmployerProfile, String> {

    Optional<EmployerProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);
}