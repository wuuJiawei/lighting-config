package io.lighting.config.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "lighting.config.client")
public class LightingClientProperties {

    private String tenant = "default";
    private String namespace = "default";
    private String appId = "default";
    private Map<String, String> labels = new LinkedHashMap<>();
    private Map<String, String> metadata = new LinkedHashMap<>();
    private List<String> bootstrapPrefixes = List.of();
    private final Server server = new Server();
    private boolean autoStart = true;
    private boolean enabled = true;
    private boolean bannerEnabled = true;
    private Duration pollInterval = Duration.ofSeconds(30);
    private String authToken;

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

    public Server getServer() {
        return server;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isBannerEnabled() {
        return bannerEnabled;
    }

    public void setBannerEnabled(boolean bannerEnabled) {
        this.bannerEnabled = bannerEnabled;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public static class Server {
        private String address = "http://localhost:7086";
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
