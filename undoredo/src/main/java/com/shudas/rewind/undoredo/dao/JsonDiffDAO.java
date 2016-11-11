package com.shudas.rewind.undoredo.dao;

import com.shudas.rewind.undoredo.model.DiffKey;
import com.shudas.rewind.undoredo.model.Diff;

import java.util.List;
import java.util.Optional;


public interface JsonDiffDAO {
    long save(String type, Diff diff);

    long saveAndDeleteLaterVersions(String type, Diff diff);

    Optional<Diff> fetchLatestDiff(String type, String keyId);

    List<Diff> fetchAllDiffs(String type, String keyId);

    List<Diff> fetchDiffsBeforeVersion(String type, DiffKey key, int numToFetch);

    List<Diff> fetchDiffsAfterVersion(String type, DiffKey key, int numToFetch);
}
