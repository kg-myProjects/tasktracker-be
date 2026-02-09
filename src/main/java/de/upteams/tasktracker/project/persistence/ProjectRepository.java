package de.upteams.tasktracker.project.persistence;

import de.upteams.tasktracker.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.projectTeam c LEFT JOIN FETCH c.appUser")
    List<Project> findAllWithTeam();
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.projectTeam c LEFT JOIN FETCH c.appUser WHERE p.id = :id")
    Optional<Project> findByIdWithTeam(@Param("id") UUID id);
    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN FETCH p.projectTeam c " +
            "LEFT JOIN FETCH p.owner " +
            "WHERE p.owner.id = :userId OR c.appUser.id = :userId")
    List<Project> findAllForUser(@Param("userId") UUID userId);



}
