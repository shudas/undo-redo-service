package com.shudas.rewind.undoredo;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.shudas.rewind.undoredo.controller.RewindController;
import com.shudas.rewind.undoredo.model.Diff;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class RewindControllerTest extends BaseTest {
    @Inject private RewindController rewindController;
    @Inject private ObjectMapper objectMapper;

    private static final int NUM_THREADS = 10;
    private ExecutorService executor;

    private DataStructures.SimpleObject o0;
    private DataStructures.SimpleObject o0_1;
    private DataStructures.SimpleObject o0_2;
    private DataStructures.SimpleObject o1;

    private DataStructures.ComplexObject c0;
    private DataStructures.ComplexObject c0_1;
    private DataStructures.ComplexObject c0_2;

    private DataStructures.ComplexObject2 co2;
    private DataStructures.ComplexObject2 co2_1;

    @Before
    public void setup() {
        // Must use new instances of objects b/c in memory javers repository keeps same instance of any old saved objects
        o0 = new DataStructures.SimpleObject();
        o0_1 = DataStructures.SimpleObject.builder()
                .s(o0.getS())
                .i(2)
                .build();
        o0_2 = DataStructures.SimpleObject.builder()
                .s("world")
                .i(o0_1.getI())
                .build();
        o1= new DataStructures.SimpleObject("world", 2);

        c0 = new DataStructures.ComplexObject(o0, 5);
        c0_1 = DataStructures.ComplexObject.builder()
                .so(o0_1)
                .ci(0)
                .build();
        c0_2 = DataStructures.ComplexObject.builder()
                .so(o0_2)
                .ci(3)
                .build();

        co2 = DataStructures.ComplexObject2.builder()
                .co(c0_1)
                .build();
        co2_1 = DataStructures.ComplexObject2.builder()
                .co(c0_2)
                .build();

        executor = Executors.newFixedThreadPool(NUM_THREADS);
    }

    @Test
    public void testSimpleJsonRead() {
        // Must use new instances of objects b/c in memory javers repository keeps same instance of any old saved objects
        saveChanges(DataStructures.SimpleObject.class, "0", o0, o0_1, o0_2);

        List<Diff> changes0 = rewindController.readJsonChanges(DataStructures.SimpleObject.class.getSimpleName(), "0");
        assertEquals(3, changes0.size());
    }

    @Test
    public void testSimpleJsonUndo() throws JsonProcessingException {
        // Must use new instances of objects b/c in memory javers repository keeps same instance of any old saved objects
        saveChanges(DataStructures.SimpleObject.class, "0", o0, o0_1, o0_2);

        Pair<Long, JsonNode> n0_1_recovered = rewindController.undo(DataStructures.SimpleObject.class.getSimpleName(), "0", 1);
        DataStructures.SimpleObject recovered = objectMapper.treeToValue(n0_1_recovered.getValue(), DataStructures.SimpleObject.class);
        assertEquals(new Long(2), n0_1_recovered.getKey());
        assertEquals(o0_1, recovered);
    }

    @Test
    public void testSimpleJsonRedo() throws JsonProcessingException {
        saveChanges(DataStructures.SimpleObject.class, "0", o0, o0_1, o0_2);

        Pair<Long, JsonNode> n0_1_recovered = rewindController.undo(DataStructures.SimpleObject.class.getSimpleName(), "0", 1);
        Pair<Long, JsonNode> redo = rewindController.redo(DataStructures.SimpleObject.class.getSimpleName(), "0", 1);
        DataStructures.SimpleObject redoObj = objectMapper.treeToValue(redo.getValue(), DataStructures.SimpleObject.class);
        assertEquals(new Long(2), n0_1_recovered.getKey());
        assertEquals(new Long(3), redo.getKey());
        assertEquals(o0_2, redoObj);
    }

    @Test
    public void testComplexJsonUndo() throws JsonProcessingException {
        saveChanges(DataStructures.ComplexObject.class, "0", c0, c0_1, c0_2);

        Pair<Long, JsonNode> n0_1_recovered = rewindController.undo(DataStructures.ComplexObject.class.getSimpleName(), "0", 2);
        DataStructures.ComplexObject recovered = objectMapper.treeToValue(n0_1_recovered.getValue(), DataStructures.ComplexObject.class);
        assertEquals(new Long(1), n0_1_recovered.getKey());
        assertEquals(c0, recovered);
    }

    @Test
    public void testComplexJsonRedo() throws JsonProcessingException {
        testRedo(c0_1, c0_2, DataStructures.ComplexObject.class);
    }

    @Test
    public void testSameObject() {
        saveChanges(DataStructures.SimpleObject.class, "0", o0_1, o0_1);
        List<Diff> changes0 = rewindController.readJsonChanges(DataStructures.SimpleObject.class.getSimpleName(), "0");
        assertEquals(1, changes0.size());
    }

    @Test
    public void testUndoThenUpdate() throws JsonProcessingException {
        DataStructures.SimpleObject s0 = new DataStructures.SimpleObject("a", 0);
        DataStructures.SimpleObject s1 = new DataStructures.SimpleObject("b", 0);
        DataStructures.SimpleObject s2 = new DataStructures.SimpleObject("c", 0);
        DataStructures.SimpleObject s3 = new DataStructures.SimpleObject("d", 0);
        saveChanges(DataStructures.SimpleObject.class, "0", s0, s1, s2, s3);
        // undo
        Pair<Long, JsonNode> s1_recovered = rewindController.undo(DataStructures.SimpleObject.class.getSimpleName(), "0", 2);
        DataStructures.SimpleObject recovered = objectMapper.treeToValue(s1_recovered.getValue(), DataStructures.SimpleObject.class);
        assertEquals(new Long(2), s1_recovered.getKey());
        assertEquals(s1, recovered);
        // update
        DataStructures.SimpleObject s4 = new DataStructures.SimpleObject(s1.getS(), 1);
        DataStructures.SimpleObject s5 = new DataStructures.SimpleObject(s1.getS(), 2);
        saveChanges(DataStructures.SimpleObject.class, "0", s4, s5);
        // fetch latest snapshot
        s1_recovered = rewindController.undo(DataStructures.SimpleObject.class.getSimpleName(), "0", 0);
        recovered = objectMapper.treeToValue(s1_recovered.getValue(), DataStructures.SimpleObject.class);
        assertEquals(new Long(4), s1_recovered.getKey());
        assertEquals(s5, recovered);
        // undo
        s1_recovered = rewindController.undo(DataStructures.SimpleObject.class.getSimpleName(), "0", 2);
        recovered = objectMapper.treeToValue(s1_recovered.getValue(), DataStructures.SimpleObject.class);
        assertEquals(new Long(2), s1_recovered.getKey());
        assertEquals(s1, recovered);
        s1_recovered = rewindController.undo(DataStructures.SimpleObject.class.getSimpleName(), "0", 1);
        recovered = objectMapper.treeToValue(s1_recovered.getValue(), DataStructures.SimpleObject.class);
        assertEquals(new Long(1), s1_recovered.getKey());
        assertEquals(s0, recovered);
    }

    @Test
    public void testNoExistingRedo() throws JsonProcessingException {
        DataStructures.SimpleObject s0 = new DataStructures.SimpleObject("a", 0);
        DataStructures.SimpleObject s1 = new DataStructures.SimpleObject("b", 0);
        DataStructures.SimpleObject s2 = new DataStructures.SimpleObject("c", 0);
        DataStructures.SimpleObject s3 = new DataStructures.SimpleObject("d", 0);
        saveChanges(DataStructures.SimpleObject.class, "0", s0, s1, s2, s3);
        // undo
        Pair<Long, JsonNode> s1_recovered = rewindController.undo(DataStructures.SimpleObject.class.getSimpleName(), "0", 2);
        DataStructures.SimpleObject recovered = objectMapper.treeToValue(s1_recovered.getValue(), DataStructures.SimpleObject.class);
        assertEquals(new Long(2), s1_recovered.getKey());
        assertEquals(s1, recovered);
        // update
        DataStructures.SimpleObject s4 = new DataStructures.SimpleObject(s1.getS(), 1);
        saveChanges(DataStructures.SimpleObject.class, "0", s4);
        // redo
        s1_recovered = rewindController.redo(DataStructures.SimpleObject.class.getSimpleName(), "0", 1);
        recovered = objectMapper.treeToValue(s1_recovered.getValue(), DataStructures.SimpleObject.class);
        assertEquals(new Long(3), s1_recovered.getKey());
        assertEquals(s4, recovered);
    }

    @Test
    public void testMultiThread() throws JsonProcessingException, InterruptedException {
        char a = 'a';

        DataStructures.SimpleObject s0 = new DataStructures.SimpleObject(String.valueOf(a), 0);
        saveChanges(DataStructures.SimpleObject.class, "0", s0);

        for (int i = 0; i < NUM_THREADS; ++i) {
            a += 1;
            DataStructures.SimpleObject s = new DataStructures.SimpleObject(String.valueOf(a), i);
            executor.submit(new SaveRunnable<>(s, DataStructures.SimpleObject.class.getSimpleName(), "0"));
        }
        executor.shutdown();
        executor.awaitTermination(10L, TimeUnit.SECONDS);

        // Try to undo. Should be able to undo NUM_THREADS number of times
        assertEquals(1 + NUM_THREADS, rewindController.readJsonChanges(DataStructures.SimpleObject.class.getSimpleName(), "0").size());
        Pair<Long, JsonNode> undo = rewindController.undo(DataStructures.SimpleObject.class.getSimpleName(), "0", NUM_THREADS);
        DataStructures.SimpleObject recovered = objectMapper.treeToValue(undo.getValue(), DataStructures.SimpleObject.class);
        assertEquals(new Long(1), undo.getKey());
        assertEquals(s0, recovered);
    }

    @AllArgsConstructor
    private class SaveRunnable<T> implements Runnable {
        T obj;
        String type, id;

        @Override
        public void run() {
            JsonNode n = objectMapper.valueToTree(obj);
            rewindController.saveNewObject(type, id, n);
        }
    }

    private void testRedo(Object o1, Object o2, Class type) throws JsonProcessingException {
        JsonNode n0_1 = objectMapper.valueToTree(o1);
        JsonNode n0_2 = objectMapper.valueToTree(o2);

        rewindController.saveNewObject(type.getSimpleName(), "0", n0_1);
        rewindController.saveNewObject(type.getSimpleName(), "0", n0_2);

        Pair<Long, JsonNode> n0_1_recovered = rewindController.undo(type.getSimpleName(), "0", 1);
        JsonNode redo = rewindController.redo(type.getSimpleName(), "0", 1).getValue();
        Object redoObj = objectMapper.treeToValue(redo, type);
        assertEquals(new Long(1), n0_1_recovered.getKey());
        assertEquals(o2, redoObj);
    }

    private void saveChanges(Class type, String id, Object...objects) {
        for (Object object : objects) {
            JsonNode n0 = objectMapper.valueToTree(object);
            rewindController.saveNewObject(type.getSimpleName(), id, n0);
        }
    }
}
