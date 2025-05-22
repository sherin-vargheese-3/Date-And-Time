package com.exercise.DateAndTime.controller;

import com.exercise.DateAndTime.dtos.TimeEntryDTO;
import com.exercise.DateAndTime.service.TimeEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/report")
    public List<TimeEntryDTO> report(
            @RequestParam Long employeeId,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam String timeZone
    ) {
        ZonedDateTime fromZdt = ZonedDateTime.parse(from);
        ZonedDateTime toZdt = ZonedDateTime.parse(to);
        return timeEntryService.getTimeEntriesBetween(employeeId, fromZdt, toZdt, timeZone);
    }

    @GetMapping("/total-duration")
    public String totalDuration(
            @RequestParam Long employeeId,
            @RequestParam String date,
            @RequestParam String timeZone,
            @RequestParam String type
    ) {
        return timeEntryService.calculateTotalDuration(employeeId, date, timeZone, type);
    }

    @GetMapping("/overlaps")
    public List<TimeEntryDTO> overlaps(
            @RequestParam Long employeeId,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam String timeZone
    ) {
        ZonedDateTime startZdt = ZonedDateTime.parse(start);
        ZonedDateTime endZdt = ZonedDateTime.parse(end);
        return timeEntryService.findOverlappingEntries(employeeId, startZdt, endZdt, timeZone);
    }
}
