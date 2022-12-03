package org.home.test.task.tracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TasksStateDTO {

    @NonNull
    Long id;

    @NonNull
    String name;

    @NonNull
    @JsonProperty("creation_time")
    Instant creationTime;

    @JsonProperty("previous_tasks_state_id")
    Long previousTasksSateId;

    @JsonProperty("next_tasks_state_id")
    Long nextTasksStateId;

    @NonNull
    List<TaskDTO> tasks;
}
