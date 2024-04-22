package ru.practicum.mainservice.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CompilationDto;
import ru.practicum.mainservice.dto.NewCompilationDto;
import ru.practicum.mainservice.dto.UpdateCompilationRequest;
import ru.practicum.mainservice.exception.EntityNotFoundException;
import ru.practicum.mainservice.mapper.CompilationMapper;
import ru.practicum.mainservice.model.Compilation;
import ru.practicum.mainservice.repository.CompilationRepository;
import ru.practicum.mainservice.repository.EventRepository;

import javax.validation.ValidationException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.mainservice.common.PageableFactory.getPageable;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CompilationDto add(NewCompilationDto newCompilationDto) {
        log.debug("Добавление новой подборки: {}", newCompilationDto);

        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);
        if (newCompilationDto.getEvents() != null) {
            compilation.setEvents(new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents())));
        }

        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Transactional
    @Override
    public void delete(Long compId) {
        log.debug("Удаление подборки с id={}", compId);

        findCompilationOrThrow(compId);

        compilationRepository.deleteById(compId);
    }

    @Transactional
    @Override
    public CompilationDto update(UpdateCompilationRequest updateCompilationRequest, Long compId) {
        log.debug("Обновление подоборки с id={}: {}", compId, updateCompilationRequest);

        Compilation compilation = findCompilationOrThrow(compId);
        if (updateCompilationRequest.getEvents() != null) {
            compilation.setEvents(new HashSet<>(eventRepository.findAllById(updateCompilationRequest.getEvents())));
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        String title = updateCompilationRequest.getTitle();
        if (title != null) {
            if (!title.isEmpty() && title.length() <= 50) {
                compilation.setTitle(title);
            } else {
                throw new ValidationException("Заголовок подборки должен содержать от 1 до 50 символов.");
            }
        }


        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }


    @Override
    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        log.debug("Получение списка подборок с параметрами: {}, {}, {}", pinned, from, size);

        if (pinned != null) {
            return compilationRepository.findAllByPinned(pinned, getPageable(from, size)).stream()
                    .map(compilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        } else {
            return compilationRepository.findAll(getPageable(from, size)).stream()
                    .map(compilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public CompilationDto getById(Long compId) {
        log.debug("Получение подборки с id={}", compId);

        return compilationMapper.toCompilationDto(findCompilationOrThrow(compId));
    }

    private Compilation findCompilationOrThrow(long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException("Подборка с id=" + compId + " не найдена."));
    }
}
