package com.shudas.rewind.undoredo.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.shudas.rewind.undoredo.model.Diff;
import javafx.util.Pair;

import java.util.List;

public interface RewindController {
    List<Diff> readJsonChanges(String type, String id);

    Pair<Long, JsonNode> undo(String type, String id, int numUndo);

    Pair<Long, JsonNode> redo(String type, String id, int numRedo);

    void saveNewObject(String type, String id, JsonNode current);
}
