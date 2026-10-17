package com.thiagoxavier.taskmanager.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fields
) {
    public static ApiError of(HttpStatus status, String message, String path) {
        return new ApiError(OffsetDateTime.now(), status.value(), status.getReasonPhrase(), message, path, Map.of());
    }

    public static ApiError withFields(HttpStatus status, String message, String path, Map<String, String> fields) {
        return new ApiError(OffsetDateTime.now(), status.value(), status.getReasonPhrase(), message, path, fields);
    }
}
