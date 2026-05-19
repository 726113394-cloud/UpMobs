package io.Sriptirc_wp_1258.gost.managers;
import io.Sriptirc_wp_1258.gost.Gost;
public class HeartbeatManager {
    private final Gost plugin;
    private boolean heartbeatEnabled = false;
    public HeartbeatManager(Gost plugin) {
        this.plugin = plugin;
    }
    public void toggleHeartbeat(boolean enabled) {
        this.heartbeatEnabled = enabled;
    }
    public void startHeartbeat() {}
    public void stopHeartbeat() {}
    public boolean isHeartbeatEnabled() {
        return heartbeatEnabled;
    }
    public void reload() {}
}