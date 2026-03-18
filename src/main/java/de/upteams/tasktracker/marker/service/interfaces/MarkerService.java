package de.upteams.tasktracker.marker.service.interfaces;

import de.upteams.tasktracker.task.entity.Task;

import java.util.List;

public interface MarkerService {
    void syncTaskMarkers(Task task, List<String> markerIds);
    void deleteTaskMarkers(String markerId);
}
