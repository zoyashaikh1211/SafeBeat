
package com.safebeat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.safebeat.model.AssessmentRecord;
import com.safebeat.model.AssessmentRequest;
import com.safebeat.model.AssessmentResponse;
import com.safebeat.repository.AssessmentRecordRepository;
import com.safebeat.service.AssessmentService;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTests {

    @Mock
    private AssessmentRecordRepository repository;

    private AssessmentService service;

    @BeforeEach
    void setUp() {
        service = new AssessmentService(
                repository, 50, 120, 92, 120);
    }

    @Test
    void freshNormalReadingsReturnNormalAndAreSaved() {
        when(repository.save(any(AssessmentRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssessmentResponse response = service.assess(
                request(75, 98, Instant.now(), false));

        assertEquals("NORMAL", response.status());
        assertNotNull(response.evaluatedAt());
        verify(repository).save(any(AssessmentRecord.class));
    }

    @Test
    void highHeartRateReturnsAlert() {
        when(repository.save(any(AssessmentRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssessmentResponse response = service.assess(
                request(130, 98, Instant.now(), false));

        assertEquals("ALERT", response.status());
        verify(repository).save(any(AssessmentRecord.class));
    }

    @Test
    void sosOverridesNormalReadings() {
        when(repository.save(any(AssessmentRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssessmentResponse response = service.assess(
                request(75, 98, Instant.now(), true));

        assertEquals("SOS", response.status());
        verify(repository).save(any(AssessmentRecord.class));
    }

    @Test
    void staleReadingsReturnMonitoringUnavailable() {
        when(repository.save(any(AssessmentRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssessmentResponse response = service.assess(
                request(75, 98, Instant.now().minusSeconds(300), false));

        assertEquals("MONITORING_UNAVAILABLE", response.status());
        assertEquals(
                "Reading is stale. Monitoring is unavailable.",
                response.message());
        assertEquals(true, response.readingAgeSeconds() >= 300);
        verify(repository).save(any(AssessmentRecord.class));
    }

    @Test
    void missingHeartRateReturnsMonitoringUnavailable() {
        when(repository.save(any(AssessmentRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssessmentResponse response = service.assess(
                request(null, 98, Instant.now(), false));

        assertEquals("MONITORING_UNAVAILABLE", response.status());
        assertEquals(
                "Required readings or timestamp are missing.",
                response.message());
        verify(repository).save(any(AssessmentRecord.class));
    }

    @Test
    void missingTimestampReturnsMonitoringUnavailableWithoutSaving() {
        AssessmentResponse response = service.assess(
                request(75, 98, null, false));

        assertEquals("MONITORING_UNAVAILABLE", response.status());
        verifyNoInteractions(repository);
    }

    private AssessmentRequest request(
            Integer heartRate,
            Integer oxygen,
            Instant recordedAt,
            boolean sosTriggered) {

        return new AssessmentRequest(
                heartRate,
                oxygen,
                recordedAt,
                sosTriggered);
    }
}
