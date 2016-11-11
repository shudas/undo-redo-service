package com.shudas.rewind.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.mongodb.MongoClient;
import com.shudas.rewind.commons.Properties;
import com.shudas.rewind.undoredo.UndoRedoModule;
import com.shudas.rewind.webapp.resource.RewindResource;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.Map;

@Slf4j
@Singleton
public class WebServerModule extends ServletModule {
    @Override
    protected void configureServlets() {
        log.info("Configuring Web Server Module");
        // Filters
        filter("/*").through(LoggingFilter.class);

        // Other modules
        install(new UndoRedoModule());

        // Resources
        bind(RewindResource.class);

        // Serve!
        bind(HttpServletDispatcher.class).in(Scopes.SINGLETON);
        Map<String, String> initParams = ImmutableMap.of(
                "resteasy.servlet.mapping.prefix", "/");
        serve("/*").with(HttpServletDispatcher.class, initParams);
    }

    @Provides
    @Singleton
    Morphia provideMorphia() {
        Morphia morphia = new Morphia();
        return morphia;
    }

    @Provides @Singleton
    ObjectMapper provideMapper() {
        return new ObjectMapper();
    }

    @Provides @Singleton
    Datastore provideDatastore(Morphia morphia) {
        // create the Datastore connecting to the default port on the local host
        final Datastore datastore = morphia.createDatastore(
                new MongoClient(Properties.PROP_MONGO_HOST, Properties.PROP_MONGO_PORT), Properties.PROP_MONGO_DB_NAME);
        datastore.ensureIndexes();
        return datastore;
    }
}
