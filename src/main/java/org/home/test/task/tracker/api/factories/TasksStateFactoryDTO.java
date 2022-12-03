package org.home.test.task.tracker.api.factories;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.home.test.task.tracker.api.dto.TasksStateDTO;
import org.home.test.task.tracker.store.entities.TasksStateEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class TasksStateFactoryDTO {

    TaskFactoryDTO taskFactoryDTO;

    public TasksStateDTO createTasksStateDTO(TasksStateEntity entity) {

        return TasksStateDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .creationTime(entity.getCreationTime())
                .previousTasksSateId(entity.getPreviousTasksState().map(TasksStateEntity::getId).orElse(null))
                .nextTasksStateId(entity.getNextTasksState().map(TasksStateEntity::getId).orElse(null))
                .tasks(entity
                                .getTasks()
                                .stream()
                                .map(taskFactoryDTO::createTaskDTO)
                                .collect(Collectors.toList())
                )
                .build();
    }
}