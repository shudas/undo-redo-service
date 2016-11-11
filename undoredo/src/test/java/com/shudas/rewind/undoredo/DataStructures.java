package com.shudas.rewind.undoredo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by shuvajit.das on 10/20/16.
 */
public class DataStructures {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimpleObject {
        private String s;
        private int i;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComplexObject {
        private SimpleObject so;
        private int ci;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComplexObject2 {
        private ComplexObject co;
    }
}
