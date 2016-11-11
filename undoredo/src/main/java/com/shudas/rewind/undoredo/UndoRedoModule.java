package com.shudas.rewind.undoredo;

import com.google.inject.AbstractModule;
import com.shudas.rewind.undoredo.controller.KeyLock;
import com.shudas.rewind.undoredo.controller.RewindController;
import com.shudas.rewind.undoredo.controller.RewindControllerImpl;
import com.shudas.rewind.undoredo.controller.StripedKeyLock;
import com.shudas.rewind.undoredo.dao.JsonDiffDAO;
import com.shudas.rewind.undoredo.dao.JsonDiffDAOMongo;
import com.shudas.rewind.undoredo.dao.SnapshotDAO;
import com.shudas.rewind.undoredo.dao.SnapshotDAOMongo;


public class UndoRedoModule extends AbstractModule {
    @Override
    protected void configure() {
        // Interfaces
        bind(RewindController.class).to(RewindControllerImpl.class);
        bind(JsonDiffDAO.class).to(JsonDiffDAOMongo.class);
        bind(SnapshotDAO.class).to(SnapshotDAOMongo.class);
        bind(KeyLock.class).to(StripedKeyLock.class);

    }
}
