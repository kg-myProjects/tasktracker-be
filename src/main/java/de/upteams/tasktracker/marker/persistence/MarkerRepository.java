package de.upteams.tasktracker.marker.persistence;

import de.upteams.tasktracker.marker.entity.Marker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MarkerRepository extends JpaRepository<Marker, UUID> {
    List<Marker> findAllByProjectId(UUID projectId);
}

