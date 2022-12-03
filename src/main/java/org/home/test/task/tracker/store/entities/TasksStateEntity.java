package org.home.test.task.tracker.store.entities;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "tasks_state")
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class TasksStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    String name;

    @Builder.Default
    Instant creationTime = Instant.now();

    @OneToOne
    TasksStateEntity previousTasksState;

    @OneToOne
    TasksStateEntity nextTasksState;

    @ManyToOne
    ProjectEntity project;

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "tasks_state_id", referencedColumnName = "id")
    List<TaskEntity> tasks = new ArrayList<>();

    public Optional<TasksStateEntity> getPreviousTasksState() {
        return Optional.ofNullable(previousTasksState);
    }

    public Optional<TasksStateEntity> getNextTasksState() {
        return Optional.ofNullable(nextTasksState);
    }
}
