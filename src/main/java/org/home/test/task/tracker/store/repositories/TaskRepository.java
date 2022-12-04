package org.home.test.task.tracker.store.repositories;

import org.home.test.task.tracker.store.entities.TaskEntity;
import org.home.test.task.tracker.store.entities.TasksStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    Optional<TaskEntity> findTaskEntityByTasksStateIdAndNameContainsIgnoreCase(Long tasksState_id, String name);

}
