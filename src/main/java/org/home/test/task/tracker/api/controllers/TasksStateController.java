package org.home.test.task.tracker.api.controllers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.home.test.task.tracker.api.controllers.helpers.ControllerHelper;
import org.home.test.task.tracker.api.dto.AckDTO;
import org.home.test.task.tracker.api.dto.TasksStateDTO;
import org.home.test.task.tracker.api.exceptions.BadRequestException;
import org.home.test.task.tracker.api.exceptions.NotFoundException;
import org.home.test.task.tracker.api.factories.TasksStateFactoryDTO;
import org.home.test.task.tracker.store.entities.ProjectEntity;
import org.home.test.task.tracker.store.entities.TasksStateEntity;
import org.home.test.task.tracker.store.repositories.TasksStateRepository;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@RestController
public class TasksStateController {

    TasksStateRepository tasksStateRepository;
    TasksStateFactoryDTO tasksStateFactoryDTO;
    ControllerHelper controllerHelper;

    public static final String CREATE_TASKS_STATE = "/api/projects/{project_id}/tasks-state";
    public static final String GET_TASKS_STATES = "/api/projects/{project_id}/tasks-states";
    public static final String UPDATE_TASKS_STATE = "/api/tasks-states/{tasks_state_id}";
    public static final String SHIFT_TASKS_STATES_POSITION = "/api/tasks-states/{tasks_state_id}/position/shift";
    public static final String DELETE_TASKS_STATE = "/api/tasks-states/{tasks_state_id}";

    @GetMapping(GET_TASKS_STATES)
    public List<TasksStateDTO> getTasksStates(@PathVariable(name = "project_id") Long projectId) {

        ProjectEntity project = controllerHelper.getProject(projectId);

        return project
                .getTasksStates()
                .stream()
                .map(tasksStateFactoryDTO::buildTasksStateDTO)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_TASKS_STATE)
    public TasksStateDTO createTasksState(
            @PathVariable(name = "project_id") Long projectId,
            @RequestParam(name = "tasks_state_name") String tasksStateName) {

        if (tasksStateName.isBlank()) {
            throw new BadRequestException("Task state name can't be empty");
        }

        Optional<TasksStateEntity> optionalAnotherTasksState = Optional.empty();

        ProjectEntity project = controllerHelper.getProject(projectId);

        for (TasksStateEntity tasksState : project.getTasksStates()) {
            if (tasksState.getName().equalsIgnoreCase(tasksStateName)) {
                throw new BadRequestException(String.format("Task state \"%s\" already exists", tasksStateName));
            }
            if (tasksState.getNextTasksState().isEmpty()) {
                optionalAnotherTasksState = Optional.of(tasksState);
                break;
            }
        }

        TasksStateEntity tasksState = tasksStateRepository.saveAndFlush(
                TasksStateEntity.builder()
                        .name(tasksStateName)
                        .project(project)
                        .build()
        );

        optionalAnotherTasksState
                .ifPresent(anotherTasksState -> {
                    tasksState.setPreviousTasksState(anotherTasksState);
                    anotherTasksState.setNextTasksState(tasksState);
                    tasksStateRepository.saveAndFlush(anotherTasksState);
                });

        final TasksStateEntity tasksStateSaved = tasksStateRepository.saveAndFlush(tasksState);

        return tasksStateFactoryDTO.buildTasksStateDTO(tasksStateSaved);
    }

    @PatchMapping(UPDATE_TASKS_STATE)
    public TasksStateDTO updateTasksState(
            @PathVariable(name = "tasks_state_id") Long tasksStateId,
            @RequestParam(name = "tasks_state_name") String tasksStateName) {

        if (tasksStateName.isBlank()) {
            throw new BadRequestException("Tasks state name can't be empty");
        }

        TasksStateEntity tasksState = getTasksState(tasksStateId);

        tasksStateRepository
                .findTasksStateEntityByProjectIdAndNameContainsIgnoreCase(
                        tasksState.getProject().getId(),
                        tasksStateName)
                .filter(anotherTasksState -> !anotherTasksState.getId().equals(tasksStateId))
                .ifPresent(anotherTasksState -> {
                    throw new BadRequestException(String.format("Tasks state \"%s\" already exists", tasksStateName));
                });

        tasksState.setName(tasksStateName);

        tasksState = tasksStateRepository.saveAndFlush(tasksState);

        return tasksStateFactoryDTO.buildTasksStateDTO(tasksState);
    }

    @PatchMapping(SHIFT_TASKS_STATES_POSITION)
    public TasksStateDTO shiftTasksStatePosition(
            @PathVariable(name = "tasks_state_id") Long tasksStateId,
            @RequestParam(name = "previous_tasks_state_id", required = false) Optional<Long> optionalPreviousTasksStateId) {

        TasksStateEntity tasksState = getTasksState(tasksStateId);

        ProjectEntity project = tasksState.getProject();

        if (tasksState.getPreviousTasksState().map(TasksStateEntity::getId).equals(optionalPreviousTasksStateId)) {
            return tasksStateFactoryDTO.buildTasksStateDTO(tasksState);
        }

        Optional<TasksStateEntity> optionalPreviousTasksStateShifted = optionalPreviousTasksStateId
                .map(previousTasksStateId -> {

                    if (tasksStateId.equals(previousTasksStateId)) {
                        throw new BadRequestException("Previous tasks state ID equal to shifting tasks state ID");
                    }

                    if (!project.getId().equals(getTasksState(previousTasksStateId).getProject().getId())) {
                        throw new BadRequestException("Tasks state position can be changed only in one project");
                    }

                    return getTasksState(previousTasksStateId);
                });

        Optional<TasksStateEntity> optionalNextTasksStateShifted;
        if (optionalPreviousTasksStateShifted.isEmpty()) {
            optionalNextTasksStateShifted = project
                    .getTasksStates()
                    .stream()
                    .filter(anotherTasksState -> anotherTasksState.getPreviousTasksState().isEmpty())
                    .findAny();
        } else {
            optionalNextTasksStateShifted = optionalPreviousTasksStateShifted
                    .get()
                    .getNextTasksState();
        }

        replaceTasksStatesPosition(tasksState);

        if (optionalPreviousTasksStateShifted.isPresent()) {
            TasksStateEntity previousTasksStateShifted = optionalPreviousTasksStateShifted.get();
            previousTasksStateShifted.setNextTasksState(tasksState);
            tasksState.setPreviousTasksState(previousTasksStateShifted);
        } else {
            tasksState.setPreviousTasksState(null);
        }

        if (optionalNextTasksStateShifted.isPresent()) {
            TasksStateEntity nextTasksStateShifted = optionalNextTasksStateShifted.get();
            nextTasksStateShifted.setPreviousTasksState(tasksState);
            tasksState.setNextTasksState(nextTasksStateShifted);
        } else {
            tasksState.setNextTasksState(null);
        }

        tasksState = tasksStateRepository.saveAndFlush(tasksState);

        optionalPreviousTasksStateShifted
                .ifPresent(tasksStateRepository::saveAndFlush);

        optionalNextTasksStateShifted
                .ifPresent(tasksStateRepository::saveAndFlush);

        return tasksStateFactoryDTO.buildTasksStateDTO(tasksState);
    }

    @DeleteMapping(DELETE_TASKS_STATE)
    public AckDTO deleteTasksState(@PathVariable(name = "tasks_state_id") Long tasksStateId) {

        TasksStateEntity tasksState = getTasksState(tasksStateId);
        replaceTasksStatesPosition(tasksState);
        tasksStateRepository.delete(tasksState);

        return AckDTO.builder().answer(true).build();
    }

    private void replaceTasksStatesPosition(TasksStateEntity tasksState) {

        Optional<TasksStateEntity> optionalPreviousTasksState = tasksState.getPreviousTasksState();
        Optional<TasksStateEntity> optionalNextTasksState = tasksState.getNextTasksState();

        optionalPreviousTasksState
                .ifPresent(it -> {
                    it.setNextTasksState(optionalNextTasksState.orElse(null));
                    tasksStateRepository.saveAndFlush(it);
                });

        optionalNextTasksState
                .ifPresent(it -> {
                    it.setPreviousTasksState(optionalPreviousTasksState.orElse(null));
                    tasksStateRepository.saveAndFlush(it);
                });
    }

    private TasksStateEntity getTasksState(Long tasksStateId) {
        return tasksStateRepository
                .findById(tasksStateId)
                .orElseThrow(() -> new NotFoundException(String.format("TasksState with \"%s\" ID doesn't " +
                        "exist", tasksStateId))
        );
    }
}
