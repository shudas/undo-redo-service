package com.shudas.rewind.undoredo.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SaveDTO {
    JsonNode before;
    JsonNode after;

    private SaveDTO() {}
}
