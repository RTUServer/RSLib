package com.github.ipecter.rtuserver.lib.plugin.storage;

import java.util.List;

public enum StorageType {

    JSON,
    SQLITE,
    MYSQL,
    MONGODB,
    MARIADB,
    POSTGRESQL;

    public static StorageType getType(String storageType) {
        if (List.of("JSON", "SQLITE", "MYSQL", "MONGODB", "MARIADB", "POSTGRESQL").contains(storageType.toUpperCase())) {
            return StorageType.valueOf(storageType.toUpperCase());
        } else return StorageType.JSON;
    }
}
