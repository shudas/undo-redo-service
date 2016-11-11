package com.shudas.rewind.undoredo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Getter
@Builder
@AllArgsConstructor
@Entity(noClassnameStored = true)
public class Snapshot {
    @Id private String id;
    private String json;
    private Long currentVersion;

    private Snapshot() {}
}
