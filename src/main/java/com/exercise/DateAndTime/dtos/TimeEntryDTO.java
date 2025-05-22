package com.exercise.DateAndTime.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntryDTO {
    private Long id;
    private Long employeeId;
    private Long projectId;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private String description;
    private String userTimeZone;
}