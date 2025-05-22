package com.exercise.DateAndTime.repository;

import com.exercise.DateAndTime.model.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByEmployeeIdAndStartTimeBetween(Long employeeId, ZonedDateTime start, ZonedDateTime end);
    List<TimeEntry> findByEmployeeIdAndStartTimeBeforeAndEndTimeAfter(Long employeeId, ZonedDateTime end, ZonedDateTime start);
}