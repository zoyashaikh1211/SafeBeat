
package com.safebeat.model;

import java.time.Instant;

public record AssessmentResponse(
        String status,
        String message,
        Instant evaluatedAt,
        Long readingAgeSeconds
) {
}
