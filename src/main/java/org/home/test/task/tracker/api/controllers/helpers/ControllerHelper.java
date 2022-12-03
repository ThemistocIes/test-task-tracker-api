package org.home.test.task.tracker.api.controllers.helpers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.home.test.task.tracker.api.exceptions.NotFoundException;
import org.home.test.task.tracker.store.entities.ProjectEntity;
import org.home.test.task.tracker.store.repositories.ProjectRepository;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
@Transactional
public class ControllerHelper {

    ProjectRepository projectRepository;

    public ProjectEntity getProject(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(
                () -> new NotFoundException(String.format("Project \"%s\" doesn't exist", projectId))
        );
    }


}
