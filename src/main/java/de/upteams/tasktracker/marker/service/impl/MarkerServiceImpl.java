package de.upteams.tasktracker.marker.service.impl;

import de.upteams.tasktracker.marker.entity.Marker;
import de.upteams.tasktracker.marker.service.interfaces.MarkerService;
import de.upteams.tasktracker.task.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.upteams.tasktracker.marker.persistence.MarkerRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarkerServiceImpl implements MarkerService {
    private final MarkerRepository markerRepository;

    @Override
    @Transactional
    public void syncTaskMarkers(Task task, List<String> markerIds) {
        List<UUID> uuids = markerIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .map(UUID::fromString).toList();

        List<Marker> markers = markerRepository.findAllById(uuids);

        task.getMarkers().clear();
        task.getMarkers().addAll(markers);
    }

}
