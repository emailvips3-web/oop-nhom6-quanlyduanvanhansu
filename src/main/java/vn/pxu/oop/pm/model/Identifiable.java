package vn.pxu.oop.pm.model;

public interface Identifiable {
    String getId();

    default boolean hasId(String id) {
        return getId().equalsIgnoreCase(id == null ? "" : id.trim());
    }
}
