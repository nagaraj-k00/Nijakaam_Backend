package com.nijakaam.repository;

import com.nijakaam.entity.JobApplication;
import com.nijakaam.enums.ApplicationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository
        extends MongoRepository<JobApplication, String> {

    // Find all applications submitted by a worker
    List<JobApplication> findByWorkerId(String workerId);

    // Find all applications received by an employer
    List<JobApplication> findByEmployerId(String employerId);

    // Find applications for a particular job
    List<JobApplication> findByJobId(String jobId);

    // Filter applications by status
    List<JobApplication> findByStatus(ApplicationStatus status);

    // Prevent duplicate applications
    boolean existsByWorkerIdAndJobId(String workerId, String jobId);
}
