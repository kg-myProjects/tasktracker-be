package de.upteams.tasktracker.project.constants;

public final class ProjectValidationConstants {

    private ProjectValidationConstants() {
        throw new IllegalStateException("Utility class");
    }

    // total length: 3–50 characters
    public static final String TITLE_REGEX = "^[\\p{L}\\p{N}][\\p{L}\\p{N} .,:()#&_/+\\-]{2,49}$";

    public static final int TITLE_MAX_LENGTH = 50;
    public static final int TITLE_MIN_LENGTH = 3;

    public static final int DESC_MAX_LENGTH = 500;
    public static final int DESC_MIN_LENGTH = 3;
}
