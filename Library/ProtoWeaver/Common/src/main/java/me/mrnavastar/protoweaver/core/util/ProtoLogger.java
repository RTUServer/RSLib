package me.mrnavastar.protoweaver.core.util;


import lombok.Setter;

public class ProtoLogger {

    public interface IProtoLogger {
        void info(String message);
        void warn(String message);
        void error(String message);
    }

    private static IProtoLogger logger;

    public static void setLogger(IProtoLogger logger) {
        ProtoLogger.logger = logger;
        info("ProtoLogger initialized.");
    }

    public static void info(String message) {
        if (logger != null) logger.info(message);
        else System.out.println("INFO: " + message);
    }

    public static void warn(String message) {
        if (logger != null) logger.warn(message);
        else System.out.println("WARN: " + message);
    }

    public static void error(String message) {
        if (logger != null) logger.error(message);
        else System.out.println("ERR: " + message);
    }
}