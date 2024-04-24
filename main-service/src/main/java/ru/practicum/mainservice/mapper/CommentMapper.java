package ru.practicum.mainservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.mainservice.dto.CommentDto;
import ru.practicum.mainservice.dto.NewCommentDto;
import ru.practicum.mainservice.model.Comment;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class, EventMapper.class})
public interface CommentMapper {

    Comment toComment(NewCommentDto newCommentDto);

    @Mapping(target = "authorName", source = "author.name")
    @Mapping(target = "eventId", source = "event.id")
    CommentDto toCommentDto(Comment comment);
}