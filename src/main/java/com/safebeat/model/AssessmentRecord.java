
package com.safebeat.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_records")
public class AssessmentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "heart_rate_bpm")
    private Integer heartRateBpm;

    @Column(name = "oxygen_saturation_percent")
    private Integer oxygenSaturationPercent;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "sos_triggered", nullable = false)
    private boolean sosTriggered;

    @Column(name = "assessment_status", nullable = false)
    private String assessmentStatus;

    @Column(name = "assessment_message", nullable = false, length = 500)
    private String assessmentMessage;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    protected AssessmentRecord() {
        // Required by JPA.
    }

    public AssessmentRecord(
            Integer heartRateBpm,
            Integer oxygenSaturationPercent,
            Instant recordedAt,
            boolean sosTriggered,
            String assessmentStatus,
            String assessmentMessage,
            Instant evaluatedAt) {
        this.heartRateBpm = heartRateBpm;
        this.oxygenSaturationPercent = oxygenSaturationPercent;
        this.recordedAt = recordedAt;
        this.sosTriggered = sosTriggered;
        this.assessmentStatus = assessmentStatus;
        this.assessmentMessage = assessmentMessage;
        this.evaluatedAt = evaluatedAt;
    }

    public Long getId() {
        return id;
    }

    public Integer getHeartRateBpm() {
        return heartRateBpm;
    }

    public Integer getOxygenSaturationPercent() {
        return oxygenSaturationPercent;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public boolean isSosTriggered() {
        return sosTriggered;
    }

    public String getAssessmentStatus() {
        return assessmentStatus;
    }

    public String getAssessmentMessage() {
        return assessmentMessage;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }
}
