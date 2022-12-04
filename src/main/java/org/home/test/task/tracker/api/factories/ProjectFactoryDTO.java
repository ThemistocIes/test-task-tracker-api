package org.home.test.task.tracker.api.factories;

import org.home.test.task.tracker.api.dto.ProjectDTO;
import org.home.test.task.tracker.store.entities.ProjectEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjectFactoryDTO {

    public ProjectDTO buildProjectDTO(ProjectEntity entity) {

        return ProjectDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .creationTime(entity.getCreationTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
