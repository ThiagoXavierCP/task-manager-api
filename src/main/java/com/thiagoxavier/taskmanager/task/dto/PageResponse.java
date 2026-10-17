package com.thiagoxavier.taskmanager.task.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envelope de paginação estável (não expõe a estrutura interna do Page do Spring).
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
