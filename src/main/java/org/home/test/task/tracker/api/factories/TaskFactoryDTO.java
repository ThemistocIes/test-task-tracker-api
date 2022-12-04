package org.home.test.task.tracker.api.factories;

import org.home.test.task.tracker.api.dto.TaskDTO;
import org.home.test.task.tracker.store.entities.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskFactoryDTO {

    public TaskDTO buildTaskDTO(TaskEntity entity) {

        return TaskDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .creationTime(entity.getCreationTime())
                .previousTaskId(entity.getPreviousTask().map(TaskEntity::getId).orElse(null))
                .nextTaskId(entity.getNextTask().map(TaskEntity::getId).orElse(null))
                .description(entity.getDescription())
                .build();
    }
}
