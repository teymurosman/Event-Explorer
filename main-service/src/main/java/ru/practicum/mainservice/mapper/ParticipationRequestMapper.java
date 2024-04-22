package ru.practicum.mainservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.ParticipationRequest;
import ru.practicum.mainservice.model.User;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = EventMapper.class)
public interface ParticipationRequestMapper {

    ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest);

    List<ParticipationRequestDto> toListParticipationRequestDto(List<ParticipationRequest> requests);

    default Long mapEventToLong(Event event) {
        return event.getId();
    }

    default Long mapUserToLong(User user) {
        return user.getId();
    }
}
