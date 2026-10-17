package com.thiagoxavier.taskmanager.task;

import com.thiagoxavier.taskmanager.exception.ResourceNotFoundException;
import com.thiagoxavier.taskmanager.task.dto.TaskRequest;
import com.thiagoxavier.taskmanager.task.dto.TaskResponse;
import com.thiagoxavier.taskmanager.task.dto.TaskSummary;
import com.thiagoxavier.taskmanager.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public TaskResponse create(TaskRequest request, User owner) {
        Task task = new Task(request.title().trim(), request.description(),
                request.priority(), request.dueDate(), owner);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> list(User owner, TaskStatus status, TaskPriority priority,
                                   String search, Pageable pageable) {
        Specification<Task> spec = Specification.where(TaskSpecifications.ownedBy(owner.getId()))
                .and(TaskSpecifications.hasStatus(status))
                .and(TaskSpecifications.hasPriority(priority))
                .and(TaskSpecifications.titleContains(search));
        return taskRepository.findAll(spec, pageable).map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long id, User owner) {
        return TaskResponse.from(findOwned(id, owner));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request, User owner) {
        Task task = findOwned(id, owner);
        task.update(request.title().trim(), request.description(), request.priority(), request.dueDate());
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse changeStatus(Long id, TaskStatus status, User owner) {
        Task task = findOwned(id, owner);
        task.changeStatus(status);
        return TaskResponse.from(task);
    }

    @Transactional
    public void delete(Long id, User owner) {
        taskRepository.delete(findOwned(id, owner));
    }

    @Transactional(readOnly = true)
    public TaskSummary summary(User owner) {
        long todo = taskRepository.countByOwnerIdAndStatus(owner.getId(), TaskStatus.TODO);
        long inProgress = taskRepository.countByOwnerIdAndStatus(owner.getId(), TaskStatus.IN_PROGRESS);
        long done = taskRepository.countByOwnerIdAndStatus(owner.getId(), TaskStatus.DONE);
        return new TaskSummary(todo, inProgress, done, todo + inProgress + done);
    }

    /**
     * Busca sempre filtrando pelo dono: um usuário nunca enxerga tarefas de outro (retorna 404, não 403,
     * para não revelar que o id existe).
     */
    private Task findOwned(Long id, User owner) {
        return taskRepository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa " + id + " não encontrada"));
    }
}
