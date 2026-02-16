package de.upteams.tasktracker.task.persistence;

import de.upteams.tasktracker.task.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
}
