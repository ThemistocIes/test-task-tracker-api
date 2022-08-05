package org.home.test.task.tracker.store.repositories;

import org.home.test.task.tracker.store.entities.TasksCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TasksCardRepository extends JpaRepository<TasksCardEntity, Long> {


}
