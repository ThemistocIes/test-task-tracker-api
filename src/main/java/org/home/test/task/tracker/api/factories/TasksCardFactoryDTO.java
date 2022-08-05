package org.home.test.task.tracker.api.factories;

import org.home.test.task.tracker.api.dto.TasksCardDTO;
import org.home.test.task.tracker.store.entities.TasksCardEntity;
import org.springframework.stereotype.Component;

@Component
public class TasksCardFactoryDTO {

    public TasksCardDTO createTasksCardDTO(TasksCardEntity entity) {

        return TasksCardDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .creationTime(entity.getCreationTime())
                .ordinal(entity.getOrdinal())
                .build();
    }
}
