package de.upteams.tasktracker.task.service.impl;

import de.upteams.tasktracker.task.dto.request.ChecklistItemDto;
import de.upteams.tasktracker.task.entity.ChecklistItem;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.service.interfaces.CheckList;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckListServiceImpl implements CheckList {
    private final TaskMappingService mappingService;

    @Override
    @Transactional
    public void syncChecklist(Task task, List<ChecklistItemDto> dto) {
        List<ChecklistItem> currentChecklist = task.getChecklist();

        List<ChecklistItem> updatedItems = dto.stream()
                .map(itemDto -> {
                    ChecklistItem item;
                    if (itemDto.id() != null && !itemDto.id().isBlank()) {
                        item = currentChecklist.stream()
                                .filter(existing -> existing.getId().toString().equals(itemDto.id()))
                                .findFirst()
                                .orElseGet(() -> mappingService.mapDtoToChecklistItem(itemDto));

                        mappingService.updateChecklistEntityFromDto(itemDto, item);
                    } else {
                        item = mappingService.mapDtoToChecklistItem(itemDto);
                    }

                    item.setTask(task);
                    return item;
                }).toList();

        currentChecklist.clear();
        currentChecklist.addAll(updatedItems);
    }
}
