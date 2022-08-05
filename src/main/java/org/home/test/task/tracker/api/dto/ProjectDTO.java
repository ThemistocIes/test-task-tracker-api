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
public class ProjectDTO {

    @NonNull
    Long id;

    @NonNull
    String name;

    @NonNull
    @JsonProperty("creation_time")
    Instant creationTime;

    @NonNull
    @JsonProperty("update_time")
    Instant updateTime;
}
