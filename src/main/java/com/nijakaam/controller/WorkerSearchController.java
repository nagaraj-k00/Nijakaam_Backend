package com.nijakaam.controller;

import com.nijakaam.dto.request.WorkerSearchCriteria;
import com.nijakaam.dto.response.ApiResponse;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.dto.response.WorkerProfileResponse;
import com.nijakaam.service.WorkerSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workers")
public class WorkerSearchController {

    private final WorkerSearchService workerSearchService;

    public WorkerSearchController(WorkerSearchService workerSearchService) {
        this.workerSearchService = workerSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<WorkerProfileResponse>>> searchWorkers(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(required = false) Double minRate,
            @RequestParam(required = false) Double maxRate,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        WorkerSearchCriteria criteria = new WorkerSearchCriteria(
                skill, location, minExperience, maxExperience, minRate, maxRate, available, page, size, sortBy, sortDir
        );

        PagedResponse<WorkerProfileResponse> result = workerSearchService.searchWorkers(criteria);

        return ResponseEntity.ok(ApiResponse.success("Workers retrieved successfully", result));
    }
}
