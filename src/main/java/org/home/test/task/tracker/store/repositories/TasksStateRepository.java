package org.home.test.task.tracker.store.repositories;

import org.home.test.task.tracker.store.entities.TasksStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TasksStateRepository extends JpaRepository<TasksStateEntity, Long> {

    Optional<TasksStateEntity> findTasksStateEntityByProjectIdAndNameContainsIgnoreCase(
            Long projectId, String tasksStateName);

}
