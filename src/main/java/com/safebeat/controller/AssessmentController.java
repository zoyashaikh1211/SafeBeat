
package com.safebeat.controller;

import com.safebeat.model.AssessmentRequest;
import com.safebeat.model.AssessmentResponse;
import com.safebeat.service.AssessmentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assessment")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping
    public AssessmentResponse assess(@RequestBody AssessmentRequest request) {
        return assessmentService.assess(request);
    }
}
