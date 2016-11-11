package com.shudas.rewind.undoredo.controller;

import com.google.common.util.concurrent.Striped;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.shudas.rewind.commons.Properties;
import com.shudas.rewind.undoredo.model.ObjectKey;

import java.util.concurrent.locks.Lock;

@Singleton
public class StripedKeyLock implements KeyLock {
    private final Striped<Lock> locks;

    @Inject
    public StripedKeyLock() {
        this.locks = Striped.lock(Properties.NUM_LOCKS);
    }

    @Override
    public Lock get(ObjectKey key) {
        return locks.get(key);
    }
}
