package com.shudas.rewind.undoredo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor
public class DiffKey {
    private String key;
    @Setter private Long version;

    private DiffKey() {}

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DiffKey)) {
            return false;
        }
        DiffKey o2 = (DiffKey) obj;
        return Objects.equals(key, o2.getKey()) && Objects.equals(version, o2.getVersion());
    }

}
