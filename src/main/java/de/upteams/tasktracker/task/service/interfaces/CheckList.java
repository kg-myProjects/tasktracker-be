package de.upteams.tasktracker.task.service.interfaces;

import de.upteams.tasktracker.task.dto.request.ChecklistItemDto;
import de.upteams.tasktracker.task.entity.Task;

import java.util.List;

public interface CheckList {
    void syncChecklist(Task task, List<ChecklistItemDto> dto);
}
