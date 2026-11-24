package com.nijakaam.service;

import com.nijakaam.dto.request.WorkerSearchCriteria;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.dto.response.WorkerProfileResponse;

public interface WorkerSearchService {

    PagedResponse<WorkerProfileResponse> searchWorkers(WorkerSearchCriteria criteria);
}
