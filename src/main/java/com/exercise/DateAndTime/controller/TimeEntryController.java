package com.exercise.DateAndTime.controller;

import com.exercise.DateAndTime.dtos.TimeEntryDTO;
import com.exercise.DateAndTime.service.TimeEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @PostMapping
    public TimeEntryDTO createTimeEntry(@RequestBody TimeEntryDTO timeEntryDTO) {
        return timeEntryService.createTimeEntry(timeEntryDTO);
    }

    @GetMapping("/total-duration")
    public String totalDuration(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String timeZone,
            @RequestParam String type
    ) {
        return timeEntryService.calculateTotalDuration(employeeId, date, timeZone, type);
    }

    @GetMapping("/report")
    public List<TimeEntryDTO> report(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam String timeZone
    ) {
        ZoneId zone = ZoneId.of(timeZone);
        ZonedDateTime fromZdt = from.atStartOfDay(zone);
        ZonedDateTime toZdt = to.plusDays(1).atStartOfDay(zone);

        return timeEntryService.getTimeEntriesBetween(employeeId, fromZdt, toZdt, timeZone);
    }

    @GetMapping("/overlaps")
    public List<TimeEntryDTO> overlaps(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam String timeZone
    ) {
        ZoneId zone = ZoneId.of(timeZone);
        ZonedDateTime startZdt = start.atStartOfDay(zone);
        ZonedDateTime endZdt = end.plusDays(1).atStartOfDay(zone);

        return timeEntryService.findOverlappingEntries(employeeId, startZdt, endZdt, timeZone);
    }

}
