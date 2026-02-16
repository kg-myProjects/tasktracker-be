package de.upteams.tasktracker.task.service.impl;

import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.task.dto.response.AttachmentResponseDto;
import de.upteams.tasktracker.task.entity.Attachment;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.persistence.AttachmentRepository;
import de.upteams.tasktracker.task.service.interfaces.AttachmentService;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskService taskService;
    private final CollaboratorService collaboratorService;
    private final TaskMappingService mappingService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    @Transactional
    public AttachmentResponseDto upload(String taskId, MultipartFile file, AppUser user) {
        if (file.isEmpty()) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        Task task = taskService.getOrThrow(taskId);

        boolean hasPermission = collaboratorService.hasUserPermission(
                user,
                task.getProject(),
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN, ProjectRoles.MEMBER)
        );

        if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "You don't have permission to upload files to this task");
        }

        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir).toAbsolutePath().normalize();

            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            Path targetLocation = path.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Attachment attachment = new Attachment();
            attachment.setName(file.getOriginalFilename());
            attachment.setUrl("/api/v1/attachments/download/" + fileName);
            attachment.setType(determineType(file.getContentType()));
            attachment.setTask(task);

            Attachment saved = attachmentRepository.save(attachment);
            return mappingService.mapAttachmentToDto(saved);

        } catch (IOException ex) {
            throw new RestApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file. Please try again!");
        }
    }

    private String determineType(String contentType) {
        if (contentType == null) return "FILE";

        String type = contentType.toLowerCase();

        if (type.contains("image")) return "IMAGE";
        if (type.contains("pdf")) return "PDF";
        if (type.contains("officedocument") || type.contains("msword") || type.contains("word")) return "DOC";
        if (type.contains("video")) return "VIDEO";
        if (type.contains("spreadsheetml") || type.contains("excel")) return "EXCEL";

        return "FILE";
    }

    @Override
    @Transactional
    public void delete(String taskId, String attachmentId, AppUser user) {
        Attachment attachment = attachmentRepository.findById(UUID.fromString(attachmentId))
                .orElseThrow(() -> new RestApiException(HttpStatus.NOT_FOUND, "Attachment not found"));

        boolean hasPermission = collaboratorService.hasUserPermission(
                user,
                attachment.getTask().getProject(),
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN, ProjectRoles.MEMBER)
        );

        if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "You don't have permission to delete this attachment");
        }

        if (!"LINK".equals(attachment.getType())) {
            try {
                String url = attachment.getUrl();
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);

                java.nio.file.Files.deleteIfExists(filePath);
            } catch (Exception e) {
                System.err.println("Could not delete physical file: " + e.getMessage());
            }
        }
        attachmentRepository.delete(attachment);
    }
}
