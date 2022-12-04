package org.home.test.task.tracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskDTO {

    @NonNull
    Long id;

    @NonNull
    String name;

    @JsonProperty("creation_time")
    Instant creationTime;

    @NonNull
    String description;

    @JsonProperty("previous_task_id")
    Long previousTaskId;

    @JsonProperty("next_task_id")
    Long nextTaskId;
}
