package de.upteams.tasktracker.user.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class UserUtils {

    public static String normalizeUserName(String name) {
        if (name == null || name.isBlank()) return null;
        name = name.trim();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String generateUserAvatarFileName(UUID userId, MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".png";
        return userId + ext;
    }

    public static Path saveUserAvatar(MultipartFile file, Path dir, String filename) throws IOException {
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);
        Files.write(target, file.getBytes());
        return target;
    }
}
