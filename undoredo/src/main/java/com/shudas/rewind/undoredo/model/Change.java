package com.shudas.rewind.undoredo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.mongodb.morphia.annotations.Entity;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Entity(noClassnameStored = true)
public class Change {
    private List<String> before;
    private List<String> after;

    private Change() {}
}
