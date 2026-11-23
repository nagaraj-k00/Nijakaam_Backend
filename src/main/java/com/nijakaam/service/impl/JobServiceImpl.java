package com.nijakaam.service.impl;

import com.nijakaam.entity.Job;
import com.nijakaam.enums.JobStatus;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.JobRepository;
import com.nijakaam.service.JobService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;

    public JobServiceImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public Job createJob(Job job) {
        job.setStatus(JobStatus.OPEN);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Override
    public Job getJobById(String id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    @Override
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Override
    public List<Job> getJobsByEmployerId(String employerId) {
        return jobRepository.findByEmployerId(employerId);
    }

    @Override
    public List<Job> getJobsByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }

    @Override
    public Job updateJobStatus(String id, JobStatus status) {
        Job job = getJobById(id);
        job.setStatus(status);
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Override
    public void deleteJob(String id) {
        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job not found with id: " + id);
        }
        jobRepository.deleteById(id);
    }
}
