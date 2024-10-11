package me.mrnavastar.protoweaver.api.protocol;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PacketType {

    private final String type;
    private final boolean global;
    private final boolean notFound;

    /**
     * @param classType 패킷의 타입
     * @param global    다른 모든 서버에 전송할지 (false)
     * @param notFound    프록시와 서버 중에 한 곳에만 등록된 커스텀 패킷인지 (false)
     */
    public static PacketType of(Class<?> classType) {
        return new PacketType(classType.getName(), false, false);
    }

    /**
     * @param classType 패킷의 타입
     * @param global    다른 모든 서버에 전송할지
     * @param notFound    프록시와 서버 중에 한 곳에만 등록된 커스텀 패킷인지 false
     */
    public static PacketType of(Class<?> classType, boolean global) {
        return new PacketType(classType.getName(), global, false);
    }

    /**
     * @param classType 패킷의 타입
     * @param global    다른 모든 서버에 전송할지
     * @param notFound    프록시와 서버 중에 한 곳에만 등록된 커스텀 패킷인지
     */
    public static PacketType of(Class<?> classType, boolean global, boolean notFound) {
        return new PacketType(classType.getName(), global, notFound);
    }

    public Class<?> getTypeClass() {
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find class: " + type, e);
        }
    }
}
