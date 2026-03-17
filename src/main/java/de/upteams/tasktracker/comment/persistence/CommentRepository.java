package de.upteams.tasktracker.comment.persistence;

import de.upteams.tasktracker.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findAllByTaskIdOrderByCreatedAtDesc(UUID taskId);
    Optional<Comment> findByIdAndTaskId(UUID id, UUID taskId);
}
