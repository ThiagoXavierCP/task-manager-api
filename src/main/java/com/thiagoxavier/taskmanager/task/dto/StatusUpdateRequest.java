package com.thiagoxavier.taskmanager.task.dto;

import com.thiagoxavier.taskmanager.task.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Status é obrigatório")
        TaskStatus status
) {
}
