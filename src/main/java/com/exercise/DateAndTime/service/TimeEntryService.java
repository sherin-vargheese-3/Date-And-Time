package com.exercise.DateAndTime.service;

import com.exercise.DateAndTime.dtos.TimeEntryDTO;
import com.exercise.DateAndTime.model.TimeEntry;
import com.exercise.DateAndTime.repository.TimeEntryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;

    public TimeEntryDTO createTimeEntry(TimeEntryDTO dto) {
        ZoneId userZone = ZoneId.of(dto.getUserTimeZone());

        Instant startUTC = dto.getStartTime().toInstant();
        Instant endUTC = dto.getEndTime().toInstant();

        TimeEntry entry = TimeEntry.builder()
                .employeeId(dto.getEmployeeId())
                .projectId(dto.getProjectId())
                .startTime(startUTC)
                .endTime(endUTC)
                .description(dto.getDescription())
                .build();

        TimeEntry saved = timeEntryRepository.save(entry);

        return convertToDTO(saved, userZone);
    }

    private TimeEntryDTO convertToDTO(TimeEntry entry, ZoneId userZone) {
        return TimeEntryDTO.builder()
                .id(entry.getId())
                .employeeId(entry.getEmployeeId())
                .projectId(entry.getProjectId())
                .startTime(entry.getStartTime().atZone(userZone))
                .endTime(entry.getEndTime().atZone(userZone))
                .description(entry.getDescription())
                .userTimeZone(userZone.getId())
                .build();
    }

    public String calculateTotalDuration(Long employeeId, LocalDate date, String timeZone, String type) {
        ZoneId zoneId = ZoneId.of(timeZone);

        ZonedDateTime start;
        ZonedDateTime end;

        switch (type.toLowerCase()) {
            case "day":
                start = date.atStartOfDay(zoneId).withZoneSameInstant(ZoneOffset.UTC);
                end = date.plusDays(1).atStartOfDay(zoneId).withZoneSameInstant(ZoneOffset.UTC);
                break;
            case "week":
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                LocalDate startOfWeek = date.with(weekFields.dayOfWeek(), 1);
                start = startOfWeek.atStartOfDay(zoneId).withZoneSameInstant(ZoneOffset.UTC);
                end = start.plusDays(7);
                break;
            case "month":
                LocalDate firstOfMonth = date.withDayOfMonth(1);
                start = firstOfMonth.atStartOfDay(zoneId).withZoneSameInstant(ZoneOffset.UTC);
                end = start.plusMonths(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid duration type. Use 'day', 'week', or 'month'.");
        }

        Instant startInstant = start.toInstant();
        Instant endInstant = end.toInstant();

        List<TimeEntry> entries = timeEntryRepository.findByEmployeeIdAndStartTimeBetween(employeeId, startInstant, endInstant);

        Duration total = entries.stream()
                .map(e -> Duration.between(e.getStartTime(), e.getEndTime()))
                .reduce(Duration.ZERO, Duration::plus);

        long hours = total.toHours();
        long minutes = total.minusHours(hours).toMinutes();

        return String.format("Total worked hours: %d hours %d minutes", hours, minutes);
    }

    public List<TimeEntryDTO> findOverlappingEntries(Long employeeId, ZonedDateTime start, ZonedDateTime end, String timeZone) {
        ZoneId zoneId = ZoneId.of(timeZone);

        Instant startInstant = start.toInstant();
        Instant endInstant = end.toInstant();

        return timeEntryRepository.findByEmployeeIdAndStartTimeBeforeAndEndTimeAfter(employeeId, endInstant, startInstant)
                .stream()
                .map(e -> convertToDTO(e, zoneId))
                .collect(Collectors.toList());
    }

    public List<TimeEntryDTO> getTimeEntriesBetween(Long employeeId, ZonedDateTime from, ZonedDateTime to, String timeZone) {
        ZoneId zoneId = ZoneId.of(timeZone);

        Instant fromInstant = from.toInstant();
        Instant toInstant = to.toInstant();

        return timeEntryRepository.findByEmployeeIdAndStartTimeBetween(employeeId, fromInstant, toInstant).stream()
                .map(entry -> convertToDTO(entry, zoneId))
                .collect(Collectors.toList());
    }

}
