package org.home.test.task.tracker.store.entities;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.Instant;
import java.util.Optional;

@Entity
@Table(name = "task")
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    String name;

    @Builder.Default
    Instant creationTime = Instant.now();

    String description;

    @OneToOne
    TaskEntity previousTask;

    @OneToOne
    TaskEntity nextTask;

    @ManyToOne
    ProjectEntity project;

    @ManyToOne
    TasksStateEntity tasksState;

    public Optional<TaskEntity> getPreviousTask() {
        return Optional.ofNullable(previousTask);
    }

    public Optional<TaskEntity> getNextTask() {
        return Optional.ofNullable(nextTask);
    }
}
