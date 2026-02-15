package de.upteams.tasktracker.task.service.interfaces;

import de.upteams.tasktracker.task.dto.response.AttachmentResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import org.springframework.web.multipart.MultipartFile;


public interface AttachmentService {

    AttachmentResponseDto upload(String taskId, MultipartFile file, AppUser user);

    void delete(String taskId,String attachmentId, AppUser user);
}
