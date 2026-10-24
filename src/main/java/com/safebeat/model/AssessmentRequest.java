
package com.safebeat.model;

import java.time.Instant;

public record AssessmentRequest(
        Integer heartRateBpm,
        Integer oxygenSaturationPercent,
        Instant recordedAt,
        Boolean sosTriggered
) {
}
