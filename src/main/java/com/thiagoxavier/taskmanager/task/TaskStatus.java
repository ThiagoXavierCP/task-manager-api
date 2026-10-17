package com.thiagoxavier.taskmanager.task;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE;

    /**
     * Regras de transição: uma tarefa concluída não volta para "a fazer".
     */
    public boolean canTransitionTo(TaskStatus target) {
        if (this == target) {
            return true;
        }
        return switch (this) {
            case TODO -> target == IN_PROGRESS || target == DONE;
            case IN_PROGRESS -> target == TODO || target == DONE;
            case DONE -> target == IN_PROGRESS;
        };
    }
}
