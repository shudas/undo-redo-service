package com.shudas.rewind.undoredo.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiffDTO {
    JsonNode node;
    long version;

    private DiffDTO() {}
}
