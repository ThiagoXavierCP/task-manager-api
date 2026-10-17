package com.thiagoxavier.taskmanager.task;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskStatusTest {

    @Test
    void todoPodeIrParaQualquerStatus() {
        assertThat(TaskStatus.TODO.canTransitionTo(TaskStatus.IN_PROGRESS)).isTrue();
        assertThat(TaskStatus.TODO.canTransitionTo(TaskStatus.DONE)).isTrue();
    }

    @Test
    void doneNaoVoltaParaTodo() {
        assertThat(TaskStatus.DONE.canTransitionTo(TaskStatus.TODO)).isFalse();
        assertThat(TaskStatus.DONE.canTransitionTo(TaskStatus.IN_PROGRESS)).isTrue();
    }

    @Test
    void mesmoStatusEhPermitido() {
        for (TaskStatus status : TaskStatus.values()) {
            assertThat(status.canTransitionTo(status)).isTrue();
        }
    }
}
