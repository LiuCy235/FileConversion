package com.dream.change.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "libreoffice")
public class LibreOfficeConfig {

    private Home home;

    public static class Home {
        private String windows;
        private String linux;

        public String getWindows() {
            return windows;
        }

        public void setWindows(String windows) {
            this.windows = windows;
        }

        public String getLinux() {
            return linux;
        }

        public void setLinux(String linux) {
            this.linux = linux;
        }
    }

    public Home getHome() {
        return home;
    }

    public void setHome(Home home) {
        this.home = home;
    }

    public String getOfficeHome() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            return home.getWindows();
        } else if (os.contains("linux")) {
            return home.getLinux();
        } else {
            throw new RuntimeException("不支持的操作系统");
        }
    }

}