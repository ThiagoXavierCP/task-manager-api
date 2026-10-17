package com.thiagoxavier.taskmanager.task;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Filtros dinâmicos de tarefas. Cada filtro só é aplicado quando o valor foi informado.
 */
public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> ownedBy(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> titleContains(String term) {
        return (root, query, cb) -> !StringUtils.hasText(term)
                ? null
                : cb.like(cb.lower(root.get("title")), "%" + term.trim().toLowerCase() + "%");
    }
}
