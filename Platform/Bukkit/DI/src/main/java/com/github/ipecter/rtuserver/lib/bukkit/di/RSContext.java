package com.github.ipecter.rtuserver.lib.bukkit.di;

import com.github.ipecter.rtuserver.lib.core.RSFramework;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

public class RSContext {

    private static ApplicationContext context;

    public static void initialize() {
        context = new AnnotationConfigApplicationContext(Config.class); // 1
    }

    public static ApplicationContext get() {
        return context;
    }

    static class Config {
        @Bean
        public RSFramework Framework() {
            return new RSFramework();
        }
    }
}
