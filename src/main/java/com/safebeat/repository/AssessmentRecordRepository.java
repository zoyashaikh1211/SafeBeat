
package com.safebeat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.safebeat.model.AssessmentRecord;

public interface AssessmentRecordRepository
        extends JpaRepository<AssessmentRecord, Long> {
}
