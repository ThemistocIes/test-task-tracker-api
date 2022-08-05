package org.home.test.task.tracker.store.entities;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks_card")
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class TasksCardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Column(unique = true)
    String name;

    @Builder.Default
    Instant creationTime = Instant.now();

    String ordinal;

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "tasks_card_id", referencedColumnName = "id")
    List<TaskEntity> tasks = new ArrayList<>();
}
