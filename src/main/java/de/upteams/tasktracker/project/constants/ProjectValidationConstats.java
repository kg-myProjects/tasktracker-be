package de.upteams.tasktracker.project.constants;

public final class ProjectValidationConstats {

    private ProjectValidationConstats() {
        throw new IllegalStateException("Utility class");
    }

    public static final String NAME_REGEX = "[A-Z][a-zA-Z0-9 ]{2,49}";
    public static final int NAME_MAX_LENGTH = 155;
    public static final int NAME_MIN_LENGTH = 3;
}
