package ru.practicum.statsserver.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.statsdto.HitCreateRequest;
import ru.practicum.statsserver.model.EndpointHit;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatsMapper {

    EndpointHit toEndpointHit(HitCreateRequest hitCreateRequest);
}
