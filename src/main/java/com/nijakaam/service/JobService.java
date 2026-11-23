package com.nijakaam.service;

import com.nijakaam.entity.Job;
import com.nijakaam.enums.JobStatus;

import java.util.List;

public interface JobService {
    Job createJob(Job job);
    Job getJobById(String id);
    List<Job> getAllJobs();
    List<Job> getJobsByEmployerId(String employerId);
    List<Job> getJobsByStatus(JobStatus status);
    Job updateJobStatus(String id, JobStatus status);
    void deleteJob(String id);
}
