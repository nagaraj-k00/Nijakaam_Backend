package com.nijakaam.repository;

import com.nijakaam.entity.Job;
import com.nijakaam.enums.JobStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {
    List<Job> findByEmployerId(String employerId);

    List<Job> findByStatus(JobStatus status);

    List<Job> findByLocationIgnoreCase(String location);

    List<Job> findByStatusAndLocationIgnoreCase(
            JobStatus status,
            String location);
}
