package ru.practicum.mainservice.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.dto.NewCategoryDto;
import ru.practicum.mainservice.exception.DataConflictException;
import ru.practicum.mainservice.exception.EntityNotFoundException;
import ru.practicum.mainservice.mapper.CategoryMapper;
import ru.practicum.mainservice.model.Category;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.mainservice.common.PageableFactory.getPageable;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    @Override
    public CategoryDto add(NewCategoryDto newCategoryDto) {
        log.debug("Добавление новой категории: {}", newCategoryDto);

        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.toCategory(newCategoryDto)));
    }

    @Transactional
    @Override
    public void delete(Long catId) {
        log.debug("Удаление категории с id={}", catId);

        findCategoryOrThrow(catId);
        if (eventRepository.findByCategoryId(catId).isPresent()) {
            throw new DataConflictException("Невозможно удалить категорию с id=" + catId +
                    ": некоторые события к ней относятся.");
        } else {
            categoryRepository.deleteById(catId);
        }
    }

    @Transactional
    @Override
    public CategoryDto update(NewCategoryDto newCategoryDto, Long catId) {
        log.debug("Обновление категории с id={}", catId);

        Category category = findCategoryOrThrow(catId);

        category.setName(newCategoryDto.getName());

        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        log.debug("Получение списка категорий с параметрами: from={}, size={}", from, size);

        return categoryRepository.findAll(getPageable(from, size)).stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getById(Long catId) {
        log.debug("Получение категории с id={}", catId);

        return categoryMapper.toCategoryDto(findCategoryOrThrow(catId));
    }

    private Category findCategoryOrThrow(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException("Категория с id=" + catId + " не найдена."));
    }
}
