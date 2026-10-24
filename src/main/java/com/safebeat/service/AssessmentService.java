
package com.safebeat.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.safebeat.model.AssessmentRecord;
import com.safebeat.model.AssessmentRequest;
import com.safebeat.model.AssessmentResponse;
import com.safebeat.repository.AssessmentRecordRepository;

@Service
public class AssessmentService {

    private final AssessmentRecordRepository assessmentRecordRepository;

    private final int minimumHeartRate;
    private final int maximumHeartRate;
    private final int minimumOxygenSaturation;
    private final long maximumReadingAgeSeconds;

    public AssessmentService(
            AssessmentRecordRepository assessmentRecordRepository,
            @Value("${safebeat.rules.minimum-heart-rate:50}")
            int minimumHeartRate,
            @Value("${safebeat.rules.maximum-heart-rate:120}")
            int maximumHeartRate,
            @Value("${safebeat.rules.minimum-oxygen-saturation:92}")
            int minimumOxygenSaturation,
            @Value("${safebeat.rules.maximum-reading-age-seconds:120}")
            long maximumReadingAgeSeconds) {

        if (minimumHeartRate < 1
                || maximumHeartRate <= minimumHeartRate
                || minimumOxygenSaturation < 0
                || minimumOxygenSaturation > 100
                || maximumReadingAgeSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Invalid SafeBeat assessment configuration");
        }

        this.assessmentRecordRepository = assessmentRecordRepository;
        this.minimumHeartRate = minimumHeartRate;
        this.maximumHeartRate = maximumHeartRate;
        this.minimumOxygenSaturation = minimumOxygenSaturation;
        this.maximumReadingAgeSeconds = maximumReadingAgeSeconds;
    }

    public AssessmentResponse assess(AssessmentRequest request) {
        Instant now = Instant.now();
        AssessmentResponse response;

        // SOS always takes priority over ordinary assessment.
        if (request != null && Boolean.TRUE.equals(request.sosTriggered())) {
            Long age = request.recordedAt() == null
                    ? null
                    : ageInSeconds(request.recordedAt(), now);

            response = new AssessmentResponse(
                    "SOS",
                    "Emergency request received. Immediate attention is required.",
                    now,
                    age
            );

            return persistIfPossible(request, response);
        }

        if (request == null
                || request.heartRateBpm() == null
                || request.oxygenSaturationPercent() == null
                || request.recordedAt() == null) {

            response = unavailable(
                    now,
                    null,
                    "Required readings or timestamp are missing."
            );

            return persistIfPossible(request, response);
        }

        Instant recordedAt = request.recordedAt();

        if (recordedAt.isAfter(now)) {
            response = unavailable(
                    now,
                    null,
                    "Reading timestamp is in the future."
            );

            return persistIfPossible(request, response);
        }

        long age = ageInSeconds(recordedAt, now);

        if (age > maximumReadingAgeSeconds) {
            response = unavailable(
                    now,
                    age,
                    "Reading is stale. Monitoring is unavailable."
            );

            return persistIfPossible(request, response);
        }

        int heartRate = request.heartRateBpm();
        int oxygen = request.oxygenSaturationPercent();

        if (heartRate < 1 || heartRate > 300
                || oxygen < 0 || oxygen > 100) {

            response = unavailable(
                    now,
                    age,
                    "Reading values are outside accepted input ranges."
            );

            return persistIfPossible(request, response);
        }

        if (heartRate < minimumHeartRate
                || heartRate > maximumHeartRate
                || oxygen < minimumOxygenSaturation) {

            response = new AssessmentResponse(
                    "ALERT",
                    "At least one configured safety rule was triggered.",
                    now,
                    age
            );
        } else {
            response = new AssessmentResponse(
                    "NORMAL",
                    "Readings are fresh and no configured rule was triggered.",
                    now,
                    age
            );
        }

        return persistIfPossible(request, response);
    }

    private AssessmentResponse persistIfPossible(
            AssessmentRequest request,
            AssessmentResponse response) {

        // AssessmentRecord requires a recorded timestamp.
        // Return the assessment even when the timestamp is missing.
        if (request != null && request.recordedAt() != null) {
            AssessmentRecord record = new AssessmentRecord(
                    request.heartRateBpm(),
                    request.oxygenSaturationPercent(),
                    request.recordedAt(),
                    Boolean.TRUE.equals(request.sosTriggered()),
                    response.status(),
                    response.message(),
                    response.evaluatedAt()
            );

            assessmentRecordRepository.save(record);
        }

        return response;
    }

    private AssessmentResponse unavailable(
            Instant now,
            Long age,
            String message) {

        return new AssessmentResponse(
                "MONITORING_UNAVAILABLE",
                message,
                now,
                age
        );
    }

    private long ageInSeconds(Instant recordedAt, Instant now) {
        return Duration.between(recordedAt, now).getSeconds();
    }
}
