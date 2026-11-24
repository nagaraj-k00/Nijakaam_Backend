package com.nijakaam.service.impl;

import com.nijakaam.dto.request.WorkerSearchCriteria;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.dto.response.WorkerProfileResponse;
import com.nijakaam.entity.User;
import com.nijakaam.entity.WorkerProfile;
import com.nijakaam.enums.Role;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.service.WorkerSearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class WorkerSearchServiceImpl implements WorkerSearchService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "rating", "experienceYears", "hourlyRate", "createdAt", "updatedAt"
    );

    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;

    public WorkerSearchServiceImpl(MongoTemplate mongoTemplate, UserRepository userRepository) {
        this.mongoTemplate = mongoTemplate;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Authenticated user identity is missing");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public PagedResponse<WorkerProfileResponse> searchWorkers(WorkerSearchCriteria criteria) {
        User currentUser = getCurrentUser();

        // Only EMPLOYER and ADMIN roles are authorized to discover/search workers
        if (currentUser.getRole() != Role.EMPLOYER && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only employers and admins are authorized to search for workers");
        }

        validateCriteria(criteria);

        Query query = new Query();

        // 1. Filter by Skill (case-insensitive substring/regex match on skills array)
        if (criteria.getSkill() != null && !criteria.getSkill().isBlank()) {
            query.addCriteria(Criteria.where("skills").regex(Pattern.quote(criteria.getSkill().trim()), "i"));
        }

        // 2. Filter by Location (case-insensitive match)
        if (criteria.getLocation() != null && !criteria.getLocation().isBlank()) {
            query.addCriteria(Criteria.where("location").regex(Pattern.quote(criteria.getLocation().trim()), "i"));
        }

        // 3. Filter by Experience Years Range
        if (criteria.getMinExperience() != null && criteria.getMaxExperience() != null) {
            query.addCriteria(Criteria.where("experienceYears").gte(criteria.getMinExperience()).lte(criteria.getMaxExperience()));
        } else if (criteria.getMinExperience() != null) {
            query.addCriteria(Criteria.where("experienceYears").gte(criteria.getMinExperience()));
        } else if (criteria.getMaxExperience() != null) {
            query.addCriteria(Criteria.where("experienceYears").lte(criteria.getMaxExperience()));
        }

        // 4. Filter by Hourly Rate Range
        if (criteria.getMinRate() != null && criteria.getMaxRate() != null) {
            query.addCriteria(Criteria.where("hourlyRate").gte(criteria.getMinRate()).lte(criteria.getMaxRate()));
        } else if (criteria.getMinRate() != null) {
            query.addCriteria(Criteria.where("hourlyRate").gte(criteria.getMinRate()));
        } else if (criteria.getMaxRate() != null) {
            query.addCriteria(Criteria.where("hourlyRate").lte(criteria.getMaxRate()));
        }

        // 5. Filter by Availability
        if (criteria.getAvailable() != null) {
            query.addCriteria(Criteria.where("available").is(criteria.getAvailable()));
        }

        // Execute count directly in MongoDB without loading collection into memory
        long totalElements = mongoTemplate.count(query, WorkerProfile.class);

        // Sorting & Pagination
        String sortProperty = resolveSortProperty(criteria.getSortBy());
        Sort.Direction direction = "asc".equalsIgnoreCase(criteria.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), Sort.by(direction, sortProperty));

        query.with(pageable);

        // Execute paginated find directly in MongoDB
        List<WorkerProfile> profiles = mongoTemplate.find(query, WorkerProfile.class);

        // Map to sanitized response DTO (no passwords, emails, phone numbers or security secrets)
        List<WorkerProfileResponse> content = profiles.stream()
                .map(WorkerProfileResponse::fromEntity)
                .toList();

        int totalPages = criteria.getSize() > 0
                ? (int) Math.ceil((double) totalElements / criteria.getSize())
                : 0;
        boolean isLast = criteria.getPage() >= totalPages - 1 || totalPages == 0;

        return new PagedResponse<>(content, criteria.getPage(), criteria.getSize(), totalElements, totalPages, isLast);
    }

    private void validateCriteria(WorkerSearchCriteria criteria) {
        if (criteria.getPage() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page index must not be less than zero");
        }
        if (criteria.getSize() <= 0 || criteria.getSize() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be between 1 and 100");
        }
        if (criteria.getMinExperience() != null && criteria.getMinExperience() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum experience cannot be negative");
        }
        if (criteria.getMaxExperience() != null && criteria.getMaxExperience() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum experience cannot be negative");
        }
        if (criteria.getMinExperience() != null && criteria.getMaxExperience() != null
                && criteria.getMinExperience() > criteria.getMaxExperience()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum experience cannot be greater than maximum experience");
        }
        if (criteria.getMinRate() != null && criteria.getMinRate() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum hourly rate cannot be negative");
        }
        if (criteria.getMaxRate() != null && criteria.getMaxRate() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum hourly rate cannot be negative");
        }
        if (criteria.getMinRate() != null && criteria.getMaxRate() != null
                && criteria.getMinRate() > criteria.getMaxRate()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum hourly rate cannot be greater than maximum hourly rate");
        }
    }

    private String resolveSortProperty(String sortBy) {
        if (sortBy != null && ALLOWED_SORT_FIELDS.contains(sortBy)) {
            return sortBy;
        }
        return "rating";
    }
}
