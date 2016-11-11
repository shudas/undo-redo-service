package com.shudas.rewind.undoredo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.JsonPatch;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.shudas.rewind.undoredo.dao.JsonDiffDAO;
import com.shudas.rewind.undoredo.dao.SnapshotDAO;
import com.shudas.rewind.undoredo.model.*;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

@Slf4j
@Singleton
public class RewindControllerImpl implements RewindController {
    private JsonDiffDAO jsonDiffDAO;
    private SnapshotDAO snapshotDAO;
    private ObjectMapper objectMapper;
    private KeyLock locks;

    @Inject
    public RewindControllerImpl(JsonDiffDAO jsonDiffDAO,
                                SnapshotDAO snapshotDAO,
                                ObjectMapper objectMapper,
                                KeyLock locks) {
        this.jsonDiffDAO = jsonDiffDAO;
        this.snapshotDAO = snapshotDAO;
        this.objectMapper = objectMapper;
        this.locks = locks;
    }

    @Override
    public List<Diff> readJsonChanges(String type, String id) {
        return jsonDiffDAO.fetchAllDiffs(type, id);
    }

    @Override
    public Pair<Long, JsonNode> undo(String type, String id, int numUndo) {
        Lock lock = locks.get(new ObjectKey(type, id));
        try {
            lock.lock();
            return undoOrRedo(type, id, numUndo, true);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Pair<Long, JsonNode> redo(String type, String id, int numRedo) {
        Lock lock = locks.get(new ObjectKey(type, id));
        try {
            lock.lock();
            return undoOrRedo(type, id, numRedo, false);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void saveNewObject(String type, String id, JsonNode current) {
        Lock lock = locks.get(new ObjectKey(type, id));
        try {
            lock.lock();
            saveJsonChanges(type, id, current);
        } finally {
            lock.unlock();
        }
    }

    private void saveJsonChanges(String type, String id, JsonNode current) {
        Optional<Snapshot> snapshot = snapshotDAO.fetchSnapshot(type, id);
        DiffKey newKey = snapshot
                .filter(s -> s.getCurrentVersion() != null)
                .map(s -> new DiffKey(id, s.getCurrentVersion()))
                .orElse(new DiffKey(id, 0L));
        JsonNode old;
        try {
            old = (snapshot.isPresent() && snapshot.get().getJson() != null)
                    ? objectMapper.readTree(snapshot.get().getJson()) : NullNode.getInstance();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not parse snapshot json");
        }

        boolean hasDiff = saveDiff(type, newKey, old, current);
        // No diff b/t old and current object
        if (!hasDiff) {
            return;
        }
        Snapshot newSnapshot = Snapshot.builder()
                .id(id)
                .json(current.toString())
                .currentVersion(newKey.getVersion() + 1)
                .build();
        snapshotDAO.saveSnapshot(type, newSnapshot);
    }

    private Pair<Long, JsonNode> undoOrRedo(String type, String id, int num, boolean isUndo) {
        Optional<Snapshot> snapshot = snapshotDAO.fetchSnapshot(type, id);
        if (!snapshot.isPresent()) {
            throw new IllegalStateException("No snapshot found for given type and id. Type: " + type + " id: " + id);
        }
        if (snapshot.get().getCurrentVersion() == null) {
            throw new IllegalStateException("Snapshot does not have current version. Type: " + type + " id: " + id);
        }
        JsonNode current;
        try {
            current = snapshot.get().getJson() != null
                    ? objectMapper.readTree(snapshot.get().getJson()) : NullNode.getInstance();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not parse snapshot json");
        }
        DiffKey currentKey = new DiffKey(id, snapshot.get().getCurrentVersion());
        Pair<Long, JsonNode> versionToNew;
        if (isUndo) {
            versionToNew = undoDiff(type, currentKey, current, num);
        } else {
            versionToNew = redoDiff(type, currentKey, current, num);
        }
        // No undos were made
        if (currentKey.getVersion().equals(versionToNew.getKey())) {
            return versionToNew;
        }
        // Save a new snapshot b/c this is where we will store new edits/redos from
        Snapshot s = Snapshot.builder()
                .id(id)
                .json(versionToNew.getValue().toString())
                .currentVersion(versionToNew.getKey())
                .build();
        snapshotDAO.saveSnapshot(type, s);
        return versionToNew;
    }

    private boolean saveDiff(String type, DiffKey key, JsonNode old, JsonNode current) {
        ArrayNode diffToOld = (ArrayNode) JsonDiff.asJson(current, old);
        if (diffToOld.size() == 0) {
            return false;
        }
        ArrayNode diffToCurrent = (ArrayNode) JsonDiff.asJson(old, current);
        List<String> before = Lists.newArrayList();
        List<String> after = Lists.newArrayList();
        diffToOld.forEach(d -> before.add(d.toString()));
        diffToCurrent.forEach(d -> after.add(d.toString()));
        Diff fullDiff = Diff.builder()
                .diffKey(DiffKey.builder()
                        .key(key.getKey())
                        .version(key.getVersion())
                        .build())
                .timestamp(System.currentTimeMillis())
                .change(Change.builder()
                        .before(before)
                        .after(after)
                        .build()
                ).build();
        // TODO: Possibly make the deletes an offline thing and keep track of undo state elsewhere
        jsonDiffDAO.saveAndDeleteLaterVersions(type, fullDiff);
        return true;
    }

    private Pair<Long, JsonNode> undoDiff(String type, DiffKey currentKey, JsonNode current, int numUndo) {
        List<Diff> diffs = jsonDiffDAO.fetchDiffsBeforeVersion(type, currentKey, numUndo);
        JsonNode res = current.deepCopy();
        long oldestVersion = currentKey.getVersion();
        for (Diff d : diffs) {
            log.info("Applying patch: " + d);
            JsonNode changeNode;
            try {
                List<String> afterChanges = d.getChange().getBefore();
                changeNode = objectMapper.readTree(afterChanges.toString());
            } catch (IOException e) {
                throw new RuntimeException("Error parsing change into JSON");
            }
            res = JsonPatch.apply(changeNode, res);
            oldestVersion = d.getDiffKey().getVersion();
        }
        return new Pair<>(oldestVersion, res);
    }

    private Pair<Long, JsonNode> redoDiff(String type, DiffKey oldKey, JsonNode current, int numRedo) {
        List<Diff> diffs = jsonDiffDAO.fetchDiffsAfterVersion(type, DiffKey.builder()
                        .key(oldKey.getKey())
                        .version(oldKey.getVersion())
                        .build(),
                numRedo);
        long newVersion = oldKey.getVersion();
        JsonNode res = current.deepCopy();
        for (Diff d : diffs) {
            log.info("Applying patch: " + d);
            JsonNode changeNode;
            try {
                List<String> afterChanges = d.getChange().getAfter();
                changeNode = objectMapper.readTree(afterChanges.toString());
            } catch (IOException e) {
                throw new RuntimeException("Error parsing change into JSON");
            }
            res = JsonPatch.apply(changeNode, res);
            newVersion = d.getDiffKey().getVersion() + 1;
        }
        return new Pair<>(newVersion, res);
    }
}
