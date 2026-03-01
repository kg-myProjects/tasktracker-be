package de.upteams.tasktracker.taskstatus.service.interfaces;

import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusCreateDto;
import de.upteams.tasktracker.taskstatus.dto.request.TaskStatusUpdateDto;
import de.upteams.tasktracker.taskstatus.dto.response.TaskStatusResponseDto;
import de.upteams.tasktracker.taskstatus.entity.TaskStatus;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.List;
import java.util.Optional;

public interface TaskStatusService {

    TaskStatusResponseDto save(TaskStatusCreateDto newTaskStatusCreateDto);

    TaskStatusResponseDto update(TaskStatusUpdateDto dto);

    List<TaskStatusResponseDto> updateTaskStatusesOrder(List<TaskStatusUpdateDto> dtos);

    TaskStatus getOrThrow(String id);

    Optional<TaskStatus> findById(String id);



    void delete(String id, AppUser changer);

}
