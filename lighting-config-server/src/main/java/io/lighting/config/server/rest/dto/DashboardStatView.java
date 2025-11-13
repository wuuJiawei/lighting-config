package io.lighting.config.server.rest.dto;

public class DashboardStatView {

    private final String label;
    private final String value;
    private final double change;
    private final String trend;
    private final String hint;

    public DashboardStatView(String label, String value, double change, String trend, String hint) {
        this.label = label;
        this.value = value;
        this.change = change;
        this.trend = trend;
        this.hint = hint;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public double getChange() {
        return change;
    }

    public String getTrend() {
        return trend;
    }

    public String getHint() {
        return hint;
    }
}
