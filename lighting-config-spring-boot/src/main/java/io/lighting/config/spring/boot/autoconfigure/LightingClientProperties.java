package io.lighting.config.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "lighting.client")
public class LightingClientProperties {

    private String tenant = "default";
    private String namespace = "default";
    private String appId = "default";
    private Map<String, String> labels = new LinkedHashMap<>();
    private Map<String, String> metadata = new LinkedHashMap<>();
    private List<String> bootstrapPrefixes = List.of();
    private Duration watchReconnectBackoff = Duration.ofSeconds(5);
    private final Server server = new Server();
    private boolean autoStart = true;

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public List<String> getBootstrapPrefixes() {
        return bootstrapPrefixes;
    }

    public void setBootstrapPrefixes(List<String> bootstrapPrefixes) {
        this.bootstrapPrefixes = bootstrapPrefixes;
    }

    public Duration getWatchReconnectBackoff() {
        return watchReconnectBackoff;
    }

    public void setWatchReconnectBackoff(Duration watchReconnectBackoff) {
        this.watchReconnectBackoff = watchReconnectBackoff;
    }

    public Server getServer() {
        return server;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public static class Server {
        private String address = "dns://localhost:9090";
        private boolean tls = false;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public boolean isTls() {
            return tls;
        }

        public void setTls(boolean tls) {
            this.tls = tls;
        }
    }
}
