package org.home.test.task.tracker.store.entities;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Column(unique = true)
    String name;

    @Builder.Default
    Instant creationTime = Instant.now();

    @Builder.Default
    Instant updateTime = Instant.now();

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "project_id")
    List<TasksStateEntity> tasksStates = new ArrayList<>();
}
