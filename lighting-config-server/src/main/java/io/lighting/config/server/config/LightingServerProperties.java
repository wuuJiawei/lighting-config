package io.lighting.config.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "lighting.config")
public class LightingServerProperties {

    private Mode mode = Mode.STANDALONE;
    private final Storage storage = new Storage();
    private final Auth auth = new Auth();
    private final Monitoring monitoring = new Monitoring();

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Storage getStorage() {
        return storage;
    }

    public Auth getAuth() {
        return auth;
    }

    public Monitoring getMonitoring() {
        return monitoring;
    }

    public enum Mode {
        STANDALONE,
        EMBEDDED
    }

    public static class Storage {
        private String type = "jdbc";
        private final Map<String, String> settings = new LinkedHashMap<>();

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, String> getSettings() {
            return settings;
        }
    }

    public static class Auth {
        private boolean enabled = true;
        private String mode = "token";
        private final Map<String, String> options = new LinkedHashMap<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public Map<String, String> getOptions() {
            return options;
        }
    }

    public static class Monitoring {
        private int cacheMissThreshold = 5;

        public int getCacheMissThreshold() {
            return cacheMissThreshold;
        }

        public void setCacheMissThreshold(int cacheMissThreshold) {
            this.cacheMissThreshold = cacheMissThreshold;
        }
    }
}
