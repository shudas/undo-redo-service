package com.shudas.rewind.undoredo.model;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ObjectKey {
    private String type;
    private String id;

    private ObjectKey() {}

    @Override
    public int hashCode() {
        return (type + id).hashCode();
    }
}
