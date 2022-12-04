package org.home.test.task.tracker.api.controllers.helpers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.home.test.task.tracker.api.exceptions.NotFoundException;
import org.home.test.task.tracker.store.entities.ProjectEntity;
import org.home.test.task.tracker.store.entities.TasksStateEntity;
import org.home.test.task.tracker.store.repositories.ProjectRepository;
import org.home.test.task.tracker.store.repositories.TasksStateRepository;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
@Transactional
public class ControllerHelper {

    ProjectRepository projectRepository;
    TasksStateRepository tasksStateRepository;

    public ProjectEntity getProject(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(
                () -> new NotFoundException(String.format("Project \"%s\" doesn't exist", projectId))
        );
    }

    public TasksStateEntity getTasksState(Long tasksStateId) {
         return tasksStateRepository.findById(tasksStateId).orElseThrow(
                () -> new NotFoundException(String.format("Tasks state \"%s\" doesn't exist", tasksStateId))
         );
    }
}
