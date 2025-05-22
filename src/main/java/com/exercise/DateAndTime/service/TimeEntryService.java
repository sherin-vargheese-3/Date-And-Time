package com.exercise.DateAndTime.service;

import com.exercise.DateAndTime.dtos.TimeEntryDTO;
import com.exercise.DateAndTime.model.TimeEntry;
import com.exercise.DateAndTime.repository.TimeEntryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeParseException;
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
        ZonedDateTime startUTC = dto.getStartTime().withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime endUTC = dto.getEndTime().withZoneSameInstant(ZoneOffset.UTC);

        TimeEntry entry = TimeEntry.builder()
                .employeeId(dto.getEmployeeId())
                .projectId(dto.getProjectId())
                .startTime(startUTC)
                .endTime(endUTC)
                .description(dto.getDescription())
                .build();

        TimeEntry saved = timeEntryRepository.save(entry);
        return toDTO(saved, userZone);
    }

    private TimeEntryDTO toDTO(TimeEntry entry, ZoneId userZone) {
        return TimeEntryDTO.builder()
                .id(entry.getId())
                .employeeId(entry.getEmployeeId())
                .projectId(entry.getProjectId())
                .startTime(entry.getStartTime().withZoneSameInstant(userZone))
                .endTime(entry.getEndTime().withZoneSameInstant(userZone))
                .description(entry.getDescription())
                .userTimeZone(userZone.getId())
                .build();
    }

    public String calculateTotalDuration(Long employeeId, String date, String timeZone, String type) {
        ZoneId zoneId = ZoneId.of(timeZone);
        LocalDate localDate;

        try {
            localDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd");
        }

        ZonedDateTime start;
        ZonedDateTime end;

        switch (type.toLowerCase()) {
            case "day":
                start = localDate.atStartOfDay(zoneId).withZoneSameInstant(ZoneOffset.UTC);
                end = localDate.plusDays(1).atStartOfDay(zoneId).withZoneSameInstant(ZoneOffset.UTC);
                break;
            case "week":
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                LocalDate startOfWeek = localDate.with(weekFields.dayOfWeek(), 1);
                start = startOfWeek.atStartOfDay(zoneId).withZoneSameInstant(ZoneOffset.UTC);
                end = start.plusDays(7);
                break;
            case "month":
                LocalDate firstOfMonth = localDate.withDayOfMonth(1);
                start = firstOfMonth.atStartOfDay(zoneId).withZoneSameInstant(ZoneOffset.UTC);
                end = start.plusMonths(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid duration type. Use 'day', 'week', or 'month'.");
        }

        List<TimeEntry> entries = timeEntryRepository.findByEmployeeIdAndStartTimeBetween(employeeId, start, end);

        Duration total = entries.stream()
                .map(e -> Duration.between(e.getStartTime(), e.getEndTime()))
                .reduce(Duration.ZERO, Duration::plus);

        long hours = total.toHours();
        long minutes = total.minusHours(hours).toMinutes();

        return String.format("Total worked hours: %d hours %d minutes", hours, minutes);
    }

    public List<TimeEntryDTO> findOverlappingEntries(Long employeeId, ZonedDateTime start, ZonedDateTime end, String timeZone) {
        ZoneId zoneId = ZoneId.of(timeZone);
        ZonedDateTime startUTC = start.withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime endUTC = end.withZoneSameInstant(ZoneOffset.UTC);

        return timeEntryRepository.findByEmployeeIdAndStartTimeBeforeAndEndTimeAfter(employeeId, endUTC, startUTC)
                .stream()
                .map(e -> toDTO(e, zoneId))
                .collect(Collectors.toList());
    }

    public List<TimeEntryDTO> getTimeEntriesBetween(Long employeeId, ZonedDateTime from, ZonedDateTime to, String timeZone) {
        ZoneId zoneId = ZoneId.of(timeZone);
        ZonedDateTime fromUTC = from.withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime toUTC = to.withZoneSameInstant(ZoneOffset.UTC);

        return timeEntryRepository.findByEmployeeIdAndStartTimeBetween(employeeId, fromUTC, toUTC).stream()
                .map(entry -> toDTO(entry, zoneId))
                .collect(Collectors.toList());
    }
}
