package ru.practicum.mainservice.service.compilation;

import ru.practicum.mainservice.dto.CompilationDto;
import ru.practicum.mainservice.dto.NewCompilationDto;
import ru.practicum.mainservice.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto add(NewCompilationDto newCompilationDto);

    void delete(Long compId);

    CompilationDto update(UpdateCompilationRequest updateCompilationRequest, Long compId);

    List<CompilationDto> getAll(Boolean pinned, int from, int size);

    CompilationDto getById(Long compId);
}
