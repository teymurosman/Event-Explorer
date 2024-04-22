package ru.practicum.mainservice.dto;

import lombok.Data;

import java.util.Set;

@Data
public class CompilationDto {

    private Long id;

    private Set<EventShortDto> events;

    private boolean pinned;

    private String title;
}
