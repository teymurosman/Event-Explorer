package ru.practicum.mainservice.service.category;

import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto add(NewCategoryDto newCategoryDto);

    void delete(Long catId);

    CategoryDto update(NewCategoryDto newCategoryDto, Long catId);

    List<CategoryDto> getAll(int from, int size);

    CategoryDto getById(Long catId);
}
