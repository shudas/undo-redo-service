package com.shudas.rewind.undoredo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Data
@Builder
@AllArgsConstructor
@Entity(noClassnameStored = true)
public class Diff {
    @Id
    private DiffKey diffKey;
    private long timestamp;
    private Change change;

    private Diff() {}
}
