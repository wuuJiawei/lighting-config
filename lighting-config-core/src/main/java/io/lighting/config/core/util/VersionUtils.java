package io.lighting.config.core.util;

/**
 * Helpers around monotonically increasing versions used by config items.
 */
public final class VersionUtils {

    private VersionUtils() {
    }

    public static long nextVersion(long currentVersion) {
        return currentVersion + 1;
    }

    public static boolean isNewer(long candidate, long baseline) {
        return candidate > baseline;
    }

    public static long coalesce(long version, long fallback) {
        return version > 0 ? version : fallback;
    }
}
