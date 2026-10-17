package com.thiagoxavier.taskmanager.task;

import com.thiagoxavier.taskmanager.task.dto.*;
import com.thiagoxavier.taskmanager.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tarefas")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Cria uma tarefa")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request,
                                               @AuthenticationPrincipal User user) {
        TaskResponse created = taskService.create(request, user);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "Lista tarefas com filtros e paginação")
    public PageResponse<TaskResponse> list(
            @Parameter(description = "Filtra por status") @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Filtra por prioridade") @RequestParam(required = false) TaskPriority priority,
            @Parameter(description = "Busca no título") @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user) {
        return PageResponse.of(taskService.list(user, status, priority, search, pageable));
    }

    @GetMapping("/summary")
    @Operation(summary = "Contagem de tarefas por status")
    public TaskSummary summary(@AuthenticationPrincipal User user) {
        return taskService.summary(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma tarefa")
    public TaskResponse get(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return taskService.get(id, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de uma tarefa")
    public TaskResponse update(@PathVariable Long id,
                               @Valid @RequestBody TaskRequest request,
                               @AuthenticationPrincipal User user) {
        return taskService.update(id, request, user);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera o status de uma tarefa")
    public TaskResponse changeStatus(@PathVariable Long id,
                                     @Valid @RequestBody StatusUpdateRequest request,
                                     @AuthenticationPrincipal User user) {
        return taskService.changeStatus(id, request.status(), user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove uma tarefa")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        taskService.delete(id, user);
    }
}
