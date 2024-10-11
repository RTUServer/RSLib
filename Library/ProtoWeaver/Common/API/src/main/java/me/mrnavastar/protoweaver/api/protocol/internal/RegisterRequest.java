package me.mrnavastar.protoweaver.api.protocol.internal;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RegisterRequest {

    private final String namespace;
    private final String key;
    private final String type;
    private final boolean global;

    public RegisterRequest(String namespace, String key, Class<?> type, boolean global) {
        this.namespace = namespace;
        this.key = key;
        this.type = type.getName();
        this.global = global;
    }

    public Class<?> getClassType() {
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Invalid class name: " + type, e);
        }
    }

    public String getNamespaceKey() {
        return namespace + ":" + key;
    }
}
