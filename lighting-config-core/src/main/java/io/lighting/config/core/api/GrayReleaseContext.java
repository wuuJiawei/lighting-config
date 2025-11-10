package io.lighting.config.core.api;

import io.lighting.config.core.dto.ClientMetadata;
import io.lighting.config.core.model.ConfigItem;
import io.lighting.config.core.model.LabelSet;

import java.util.Objects;

/**
 * Carries request-time data when evaluating gray/label routing.
 */
public final class GrayReleaseContext {

    private final ConfigItem item;
    private final LabelSet labels;
    private final ClientMetadata clientMetadata;

    private GrayReleaseContext(Builder builder) {
        this.item = Objects.requireNonNull(builder.item, "item");
        this.labels = builder.labels == null ? LabelSet.empty() : builder.labels;
        this.clientMetadata = Objects.requireNonNull(builder.clientMetadata, "clientMetadata");
    }

    public static Builder builder() {
        return new Builder();
    }

    public ConfigItem getItem() {
        return item;
    }

    public LabelSet getLabels() {
        return labels;
    }

    public ClientMetadata getClientMetadata() {
        return clientMetadata;
    }

    public static final class Builder {
        private ConfigItem item;
        private LabelSet labels = LabelSet.empty();
        private ClientMetadata clientMetadata = ClientMetadata.builder().build();

        public Builder item(ConfigItem item) {
            this.item = item;
            return this;
        }

        public Builder labels(LabelSet labels) {
            this.labels = labels;
            return this;
        }

        public Builder clientMetadata(ClientMetadata clientMetadata) {
            this.clientMetadata = clientMetadata;
            return this;
        }

        public GrayReleaseContext build() {
            return new GrayReleaseContext(this);
        }
    }
}
