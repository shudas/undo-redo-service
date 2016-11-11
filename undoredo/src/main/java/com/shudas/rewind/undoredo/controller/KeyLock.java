package com.shudas.rewind.undoredo.controller;

import com.shudas.rewind.undoredo.model.ObjectKey;

import java.util.concurrent.locks.Lock;


public interface KeyLock {
    Lock get(ObjectKey key);
}
