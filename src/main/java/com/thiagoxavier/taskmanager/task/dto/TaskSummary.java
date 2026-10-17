package com.thiagoxavier.taskmanager.task.dto;

public record TaskSummary(
        long todo,
        long inProgress,
        long done,
        long total
) {
}
