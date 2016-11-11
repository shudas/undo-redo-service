package com.shudas.rewind.undoredo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.shudas.rewind.commons.Properties;
import com.shudas.rewind.undoredo.controller.KeyLock;
import com.shudas.rewind.undoredo.controller.RewindController;
import com.shudas.rewind.undoredo.controller.RewindControllerImpl;
import com.shudas.rewind.undoredo.controller.StripedKeyLock;
import com.shudas.rewind.undoredo.dao.JsonDiffDAO;
import com.shudas.rewind.undoredo.dao.JsonDiffDAOMongo;
import com.shudas.rewind.undoredo.dao.SnapshotDAO;
import com.shudas.rewind.undoredo.dao.SnapshotDAOMongo;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;


public class BaseTestModule extends AbstractModule {
    protected Morphia morphia = new Morphia();
    protected Datastore datastore;

    @Override
    protected void configure() {
        bind(RewindController.class).to(RewindControllerImpl.class);
        bind(JsonDiffDAO.class).to(JsonDiffDAOMongo.class);
        bind(SnapshotDAO.class).to(SnapshotDAOMongo.class);
        bind(KeyLock.class).to(StripedKeyLock.class);
    }

    @Provides @Singleton
    Morphia provideMorphia() {
        return morphia;
    }

    @Provides @Singleton
    ObjectMapper provideMapper() {
        return new ObjectMapper();
    }

    @Provides
    @Singleton
    Datastore provideDatastore(Morphia morphia) {
        // create the Datastore connecting to the default port on the local host
        datastore = morphia.createDatastore(
                new MongoClient(Properties.PROP_MONGO_HOST, Properties.PROP_MONGO_PORT), Properties.PROP_MONGO_DB_NAME + "_test");
        datastore.ensureIndexes();
        return datastore;
    }

    public void cleanup() {
        datastore.getDB().dropDatabase();
    }
}
