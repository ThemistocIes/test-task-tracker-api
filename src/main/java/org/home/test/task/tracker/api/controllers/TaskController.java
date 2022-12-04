package org.home.test.task.tracker.api.controllers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.home.test.task.tracker.api.controllers.helpers.ControllerHelper;
import org.home.test.task.tracker.api.dto.AckDTO;
import org.home.test.task.tracker.api.dto.TaskDTO;
import org.home.test.task.tracker.api.exceptions.BadRequestException;
import org.home.test.task.tracker.api.exceptions.NotFoundException;
import org.home.test.task.tracker.api.factories.TaskFactoryDTO;
import org.home.test.task.tracker.store.entities.TaskEntity;
import org.home.test.task.tracker.store.entities.TasksStateEntity;
import org.home.test.task.tracker.store.repositories.TaskRepository;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@RestController
public class TaskController {
    TaskRepository taskRepository;
    TaskFactoryDTO taskFactoryDTO;
    ControllerHelper controllerHelper;

    public static final String CREATE_TASK = "/api/tasks-states/{tasks_state_id}/task";
    public static final String GET_TASK = "/api/tasks-states/{tasks_state_id}/tasks";
    public static final String UPDATE_TASK = "/api/tasks/{task_id}/";
    public static final String SHIFT_TASKS_POSITION = "/api/tasks/{task_id}/position/shift";
    public static final String DELETE_TASK = "/api/tasks/{task_id}";

    @GetMapping(GET_TASK)
    public List<TaskDTO> getTasks(@PathVariable(name = "tasks_state_id") Long tasksStateId) {

        TasksStateEntity tasksState = controllerHelper.getTasksState(tasksStateId);

        return tasksState
                .getTasks()
                .stream()
                .map(taskFactoryDTO::buildTaskDTO)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_TASK)
    public TaskDTO createTask(@PathVariable(name = "tasks_state_id") Long tasksStateId,
                              @RequestParam(name = "task_name") String taskName,
                              @RequestParam(name = "description") String description) {

        if (taskName.isBlank()) {
            throw new BadRequestException("Task name is empty");
        }

        if (description.isBlank()) {
            throw new BadRequestException("Description is empty");
        }

        Optional<TaskEntity> optionalAnotherTask = Optional.empty();

        TasksStateEntity tasksState = controllerHelper.getTasksState(tasksStateId);

        for (TaskEntity task : tasksState.getTasks()) {
            if (task.getName().equalsIgnoreCase(taskName)) {
                throw new BadRequestException("Task with that name already exists");
            }
            if (task.getNextTask().isEmpty()) {
                optionalAnotherTask = Optional.of(task);
                break;
            }
        }

        TaskEntity task = taskRepository.saveAndFlush(
                TaskEntity.builder()
                        .name(taskName)
                        .description(description)
                        .project(tasksState.getProject())
                        .tasksState(tasksState)
                        .build()
        );

        optionalAnotherTask
                .ifPresent(anotherTask -> {
                    task.setPreviousTask(anotherTask);
                    anotherTask.setNextTask(task);
                    taskRepository.saveAndFlush(anotherTask);
                });

        final TaskEntity taskSaved = taskRepository.saveAndFlush(task);

        return taskFactoryDTO.buildTaskDTO(taskSaved);
    }

    @PatchMapping(UPDATE_TASK)
    public TaskDTO updateTask(@PathVariable(name = "task_id") Long taskId,
                              @RequestParam(name = "task_name") String taskName,
                              @RequestParam(name = "description") String description) {

        if (taskName.isBlank()) {
            throw new BadRequestException("Task name is empty");
        }

        if (description.isBlank()) {
            throw new BadRequestException("Description is empty");
        }

        TaskEntity task = getTask(taskId);

        taskRepository
                .findTaskEntityByTasksStateIdAndNameContainsIgnoreCase(task.getTasksState().getId(), taskName)
                .filter(anotherTask -> !anotherTask.getId().equals(taskId))
                .ifPresent(anotherTask -> {
                    throw new BadRequestException("Task with that name already exists");
                });

        task.setName(taskName);
        task.setDescription(description);
        task = taskRepository.saveAndFlush(task);

        return taskFactoryDTO.buildTaskDTO(task);
    }

    @PatchMapping(SHIFT_TASKS_POSITION)
    public TaskDTO shiftTasksPosition(@PathVariable(name = "task_id") Long taskId,
                                      @RequestParam(name = "previous_task_id", required = false)
                                      Optional<Long> optionalPreviousTaskId) {

        TaskEntity task = getTask(taskId);

        TasksStateEntity tasksState = task.getTasksState();

        if (task.getPreviousTask().map(TaskEntity::getId).equals(optionalPreviousTaskId)) {
            return taskFactoryDTO.buildTaskDTO(task);
        }

        Optional<TaskEntity> optionalPreviousTaskShifted = optionalPreviousTaskId
                .map(previousTaskId -> {

                    if (taskId.equals(previousTaskId)) {
                        throw new BadRequestException("Previous task ID equal to shifting task ID");
                    }

                    if (!tasksState.getId().equals(getTask(previousTaskId).getTasksState().getId())) {
                        throw new BadRequestException("Tasks state position can be changed only in one tasks state");
                    }

                    return getTask(previousTaskId);
                });

        Optional<TaskEntity> optionalNextTaskShifted;
        if (optionalPreviousTaskShifted.isEmpty()) {
            optionalNextTaskShifted = tasksState
                    .getTasks()
                    .stream()
                    .filter(anotherTask -> anotherTask.getPreviousTask().isEmpty())
                    .findAny();
        } else {
            optionalNextTaskShifted = optionalPreviousTaskShifted
                    .get()
                    .getNextTask();
        }

        replaceTasksPosition(task);

        if (optionalPreviousTaskShifted.isPresent()) {
            TaskEntity previousTaskShifted = optionalPreviousTaskShifted.get();
            previousTaskShifted.setNextTask(task);
            task.setPreviousTask(previousTaskShifted);
        } else {
            task.setPreviousTask(null);
        }

        if (optionalNextTaskShifted.isPresent()) {
            TaskEntity nextTaskShifted = optionalNextTaskShifted.get();
            nextTaskShifted.setPreviousTask(task);
            task.setNextTask(nextTaskShifted);
        } else {
            task.setNextTask(null);
        }

        task = taskRepository.saveAndFlush(task);

        optionalPreviousTaskShifted
                .ifPresent(taskRepository::saveAndFlush);

        optionalNextTaskShifted
                .ifPresent(taskRepository::saveAndFlush);

        return taskFactoryDTO.buildTaskDTO(task);
    }

    @DeleteMapping(DELETE_TASK)
    public AckDTO deleteTasksState(@PathVariable(name = "task_id") Long taskId) {

        TaskEntity task = getTask(taskId);
        replaceTasksPosition(task);
        taskRepository.delete(task);

        return AckDTO.builder().answer(true).build();
    }

    private void replaceTasksPosition(TaskEntity task) {

        Optional<TaskEntity> optionalPreviousTask = task.getPreviousTask();
        Optional<TaskEntity> optionalNextTask = task.getNextTask();

        optionalPreviousTask
                .ifPresent(it -> {
                    it.setNextTask(optionalNextTask.orElse(null));
                    taskRepository.saveAndFlush(it);
                });

        optionalNextTask
                .ifPresent(it -> {
                    it.setPreviousTask(optionalPreviousTask.orElse(null));
                    taskRepository.saveAndFlush(it);
                });
    }

    private TaskEntity getTask(Long taskId) {
        return taskRepository
                .findById(taskId)
                .orElseThrow(() -> new NotFoundException(String.format("Task with \"%s\" ID doesn't exist", taskId)));
    }
}
