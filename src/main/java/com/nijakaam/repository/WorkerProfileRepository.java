package com.nijakaam.repository;

import com.nijakaam.entity.WorkerProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WorkerProfileRepository extends MongoRepository<WorkerProfile, String> {

    Optional<WorkerProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);
}