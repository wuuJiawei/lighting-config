package io.lighting.config.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "lighting")
public class LightingServerProperties {

    private Mode mode = Mode.STANDALONE;
    private final Server server = new Server();
    private final Grpc grpc = new Grpc();
    private final Storage storage = new Storage();
    private final Redis redis = new Redis();
    private final Auth auth = new Auth();

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Server getServer() {
        return server;
    }

    public Grpc getGrpc() {
        return grpc;
    }

    public Storage getStorage() {
        return storage;
    }

    public Redis getRedis() {
        return redis;
    }

    public Auth getAuth() {
        return auth;
    }

    public enum Mode {
        STANDALONE,
        EMBEDDED
    }

    public static class Server {
        private int port = 8080;
        private Duration gracefulShutdown = Duration.ofSeconds(10);

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public Duration getGracefulShutdown() {
            return gracefulShutdown;
        }

        public void setGracefulShutdown(Duration gracefulShutdown) {
            this.gracefulShutdown = gracefulShutdown;
        }
    }

    public static class Grpc {
        private boolean enabled = true;
        private int port = 9090;
        private boolean tls = false;
        private Duration keepaliveTime = Duration.ofSeconds(30);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isTls() {
            return tls;
        }

        public void setTls(boolean tls) {
            this.tls = tls;
        }

        public Duration getKeepaliveTime() {
            return keepaliveTime;
        }

        public void setKeepaliveTime(Duration keepaliveTime) {
            this.keepaliveTime = keepaliveTime;
        }
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

    public static class Redis {
        private boolean enabled = false;
        private String url = "";
        private Duration ttl = Duration.ofMinutes(5);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
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
}
