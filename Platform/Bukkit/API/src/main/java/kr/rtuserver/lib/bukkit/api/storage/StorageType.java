package kr.rtuserver.lib.bukkit.api.storage;

import java.util.List;

public enum StorageType {

    JSON,
    SQLITE,
    MYSQL,
    MONGODB,
    MARIADB,
    POSTGRESQL;

    public static StorageType getType(String storageType) {
        if (List.of("JSON", "MYSQL", "MONGODB", "MARIADB").contains(storageType.toUpperCase())) {
            return StorageType.valueOf(storageType.toUpperCase());
        } else return StorageType.JSON;
    }
}
