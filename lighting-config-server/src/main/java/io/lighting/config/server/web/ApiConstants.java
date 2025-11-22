package io.lighting.config.server.web;

/**
 * Shared HTTP constants to avoid duplicated magic values across server endpoints/filters.
 */
public final class ApiConstants {

    private ApiConstants() {
    }

    public static final String API_BASE_PATH = "/lighting-config/api";
    public static final String ADMIN_CONSOLE_PATH = API_BASE_PATH + "/admin/console";
    public static final String ADMIN_CONFIG_PATH = API_BASE_PATH + "/admin/config";
    public static final String AUTH_PATH = API_BASE_PATH + "/auth";
    public static final String REVISIONS_PATH = API_BASE_PATH + "/revisions";
    public static final String OPENAPI_PATH = API_BASE_PATH + "/openapi";
    public static final String DOCS_PATH = API_BASE_PATH + "/docs";
    public static final String API_PATH_PATTERN = API_BASE_PATH + "/**";
    public static final String SWAGGER_UI_SEGMENT = "swagger-ui";

    public static final String DEFAULT_TENANT = "default";
    public static final String DEFAULT_NAMESPACE = "default";
    public static final String DEFAULT_APP_ID = "default";
    public static final String DEFAULT_ACTOR = "api";

    public static final String HEADER_AUTHORIZATION_BEARER_PREFIX = "Bearer ";
    public static final String HEADER_TOKEN = "X-Lighting-Token";
    public static final String BEARER_TOKEN_TYPE = "bearer";
    public static final String BEARER_FORMAT = "TOKEN";
    public static final String UNAUTHORIZED_MESSAGE = "Unauthorized";
}
