package com.shudas.rewind.undoredo.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.shudas.rewind.commons.StringUtil;
import com.shudas.rewind.undoredo.model.Snapshot;
import lombok.extern.slf4j.Slf4j;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Datastore;

import javax.annotation.Nonnull;
import java.util.Optional;


@Slf4j
@Singleton
public class SnapshotDAOMongo implements SnapshotDAO {
    private static final String ID_FIELD = "_id";
    private static final String SNAPSHOT_CONST = "_snapshot";

    private final AdvancedDatastore ds;

    @Inject
    public SnapshotDAOMongo(Datastore ds) {
        this.ds = (AdvancedDatastore) ds;
    }

    private String getCollectionName(@Nonnull String type) {
        return type + SNAPSHOT_CONST;
    }

    @Override
    public void saveSnapshot(String type, Snapshot snapshot) {
        if (!StringUtil.isAlphaNumeric(type)) {
            throw new IllegalArgumentException("Type must be alphanumeric. Found: " + type);
        }
        if (StringUtil.isNullOrWhitespace(snapshot.getId())) {
            throw new IllegalArgumentException("Snapshot ID cannot be null or whitespace. Found: " + snapshot.getId());
        }
        // append the snapshot word to all types
        ds.save(getCollectionName(type), snapshot);
    }

    @Override
    public Optional<Snapshot> fetchSnapshot(String type, String id) {
        if (!StringUtil.isAlphaNumeric(type)) {
            throw new IllegalArgumentException("Type must be alphanumeric. Found: " + type);
        }
        if (StringUtil.isNullOrWhitespace(id)) {
            throw new IllegalArgumentException("Snapshot ID cannot be null or whitespace. Found: " + id);
        }
        return Optional.ofNullable(ds.createQuery(getCollectionName(type), Snapshot.class)
                .field(ID_FIELD).equal(id)
                .get());
    }
}
