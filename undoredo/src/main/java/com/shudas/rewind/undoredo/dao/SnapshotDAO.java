package com.shudas.rewind.undoredo.dao;

import com.shudas.rewind.undoredo.model.Snapshot;

import java.util.Optional;

/**
 * Latest snapshot of any object
 */
public interface SnapshotDAO {
    void saveSnapshot(String type, Snapshot snapshot);

    Optional<Snapshot> fetchSnapshot(String type, String id);
}
