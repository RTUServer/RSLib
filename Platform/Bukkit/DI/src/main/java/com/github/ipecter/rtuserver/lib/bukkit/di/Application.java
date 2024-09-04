package com.github.ipecter.rtuserver.lib.bukkit.di;

import com.github.ipecter.rtuserver.lib.core.RSFramework;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

public class Application {

    public static void initialize() {
        ApplicationContext context = new AnnotationConfigApplicationContext(Config.class); // 1
    }

    static class Config {
        @Bean
        public RSFramework departmentService() {
            return new RSFramework();
        }
    }
}
