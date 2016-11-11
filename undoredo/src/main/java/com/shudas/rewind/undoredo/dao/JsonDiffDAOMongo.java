package com.shudas.rewind.undoredo.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.shudas.rewind.undoredo.model.Diff;
import com.shudas.rewind.undoredo.model.DiffKey;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.util.List;
import java.util.Optional;


@Slf4j
@Singleton
public class JsonDiffDAOMongo implements JsonDiffDAO {
    private static final String ID_FIELD = "_id";
    private static final String KEY_FIELD = "key";
    private static final String VERSION_FIELD = "version";

    private final AdvancedDatastore ds;

    @Inject
    public JsonDiffDAOMongo(Datastore ds) {
        this.ds = (AdvancedDatastore) ds;
    }

    private Query<Diff> createLatestVersionQuery(String type, String keyId) {
        return ds.createQuery(type, Diff.class)
                .field(ID_FIELD + "." + KEY_FIELD).equal(keyId)
                .order("-" + ID_FIELD + "." + VERSION_FIELD)
                .limit(1);
    }

    private Query<Diff> createIdQuery(String type, DiffKey key) {
        return ds.createQuery(type, Diff.class).field(ID_FIELD).equal(key);
    }

    @Override
    public long save(String type, Diff diff) {
        if (diff == null || diff.getDiffKey() == null) {
            throw new IllegalArgumentException("Diff or Diff key cannot be null");
        }
        // Increment version
        if (diff.getDiffKey().getVersion() == null) {
            Long newVersion = fetchLatestVersion(type, diff.getDiffKey().getKey())
                    .map(l -> l + 1)
                    .orElse(0L);
            diff.getDiffKey().setVersion(newVersion);
        }
        ds.save(type, diff);
        return diff.getDiffKey().getVersion();
    }

    @Override
    public long saveAndDeleteLaterVersions(String type, Diff diff) {
        long latestVersion = save(type, diff);
        ds.delete(ds.createQuery(type, Diff.class).field(ID_FIELD + "." + VERSION_FIELD).greaterThan(latestVersion));
        return latestVersion;
    }

    @Override
    public Optional<Diff> fetchLatestDiff(String type, String keyId) {
        if (keyId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(createLatestVersionQuery(type, keyId).get());
    }

    @Override
    public List<Diff> fetchAllDiffs(String type, String keyId) {
        return ds.createQuery(type, Diff.class)
                .field(ID_FIELD + "." + KEY_FIELD).equal(keyId)
                .order("-" + ID_FIELD + "." + VERSION_FIELD)
                .asList();
    }

    @Override
    public List<Diff> fetchDiffsBeforeVersion(String type, DiffKey key, int numToFetch) {
        if (numToFetch < 0) {
            throw new IllegalArgumentException("numToFetch must be >= 0. numToFetch: " + numToFetch);
        } else if (numToFetch == 0) {
            return Lists.newArrayList();
        }
        return ds.createQuery(type, Diff.class)
                .field(ID_FIELD + "." + KEY_FIELD).equal(key.getKey())
                .field(ID_FIELD + "." + VERSION_FIELD).lessThan(key.getVersion())
                .order("-" + ID_FIELD + "." + VERSION_FIELD)
                .limit(numToFetch <= 0 ? 0 : numToFetch)
                .asList();
    }

    @Override
    public List<Diff> fetchDiffsAfterVersion(String type, DiffKey key, int numToFetch) {
        if (numToFetch < 0) {
            throw new IllegalArgumentException("numToFetch must be >= 0. numToFetch: " + numToFetch);
        } else if (numToFetch == 0) {
            return Lists.newArrayList();
        }
        return ds.createQuery(type, Diff.class)
                .field(ID_FIELD + "." + KEY_FIELD).equal(key.getKey())
                .field(ID_FIELD + "." + VERSION_FIELD).greaterThanOrEq(key.getVersion())
                .order(ID_FIELD + "." + VERSION_FIELD)
                .limit(numToFetch <= 0 ? 0 : numToFetch)
                .asList();
    }

    private Optional<Long> fetchLatestVersion(String type, String keyId) {
        return fetchLatestDiff(type, keyId).map(d -> d.getDiffKey().getVersion());
    }
}
