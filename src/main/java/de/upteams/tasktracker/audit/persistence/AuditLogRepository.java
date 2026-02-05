package de.upteams.tasktracker.audit.persistence;

import de.upteams.tasktracker.audit.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, String> {

    List<AuditLogEntity> findAllByProjectIdOrderByCreatedAtDesc(String projectId);
    void deleteAllByProjectId(String projectId);

}
