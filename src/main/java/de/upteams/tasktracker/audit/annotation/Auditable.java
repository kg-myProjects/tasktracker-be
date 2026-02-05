package de.upteams.tasktracker.audit.annotation;

import de.upteams.tasktracker.audit.utils.AuditLogAction;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {

    String entity();
    AuditLogAction action();
    String nameField() default "";
    Class<?> entityClass() default Void.class;
}