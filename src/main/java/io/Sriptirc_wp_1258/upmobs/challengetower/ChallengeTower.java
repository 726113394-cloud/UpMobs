package io.Sriptirc_wp_1258.upmobs.challengetower;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * 挑战塔数据类
 * 表示一个挑战塔的配置和数据
 */
public class ChallengeTower {
    
    private final String id;
    private final String name;
    private final ChallengeTowerArea area;
    private final ChallengeTowerConfig config;
    private final SkyTowerConfig skyTowerConfig;
    private final ChallengeTowerManager manager;
    
    // 统计信息
    private int totalCompletions = 0;
    private int totalPlayers = 0;
    private long createdTime;
    private long lastUsedTime;
    
    public ChallengeTower(String id, String name, ChallengeTowerArea area, 
                         ChallengeTowerConfig config, SkyTowerConfig skyTowerConfig, 
                         ChallengeTowerManager manager) {
        this.id = id;
        this.name = name;
        this.area = area;
        this.config = config;
        this.skyTowerConfig = skyTowerConfig;
        this.manager = manager;
        this.createdTime = System.currentTimeMillis();
        this.lastUsedTime = System.currentTimeMillis();
    }
    
    /**
     * 从配置节加载挑战塔
     */
    public static ChallengeTower fromConfig(ConfigurationSection config, ChallengeTowerManager manager) {
        if (config == null) {
            return null;
        }
        
        String id = config.getString("id");
        String name = config.getString("name");
        
        if (id == null || name == null) {
            return null;
        }
        
        // 加载区域
        ConfigurationSection areaConfig = config.getConfigurationSection("area");
        if (areaConfig == null) {
            return null;
        }
        
        String worldName = areaConfig.getString("world");
        if (worldName == null) {
            return null;
        }
        
        org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        
        int minX = areaConfig.getInt("min_x");
        int minY = areaConfig.getInt("min_y");
        int minZ = areaConfig.getInt("min_z");
        int maxX = areaConfig.getInt("max_x");
        int maxY = areaConfig.getInt("max_y");
        int maxZ = areaConfig.getInt("max_z");
        
        Location min = new Location(world, minX, minY, minZ);
        Location max = new Location(world, maxX, maxY, maxZ);
        
        ChallengeTowerArea area = new ChallengeTowerArea(min, max, name);
        
        // 加载挑战塔配置
        ChallengeTowerConfig towerConfig = new ChallengeTowerConfig();
        if (config.contains("config")) {
            towerConfig.loadFromConfig(config.getConfigurationSection("config"));
        }
        
        // 加载通天层配置
        SkyTowerConfig skyTowerConfig = new SkyTowerConfig();
        if (config.contains("sky_tower")) {
            skyTowerConfig.loadFromConfig(config.getConfigurationSection("sky_tower"));
        }
        
        // 创建挑战塔
        ChallengeTower tower = new ChallengeTower(id, name, area, towerConfig, skyTowerConfig, manager);
        
        // 加载统计信息
        tower.totalCompletions = config.getInt("total_completions", 0);
        tower.totalPlayers = config.getInt("total_players", 0);
        tower.createdTime = config.getLong("created_time", System.currentTimeMillis());
        tower.lastUsedTime = config.getLong("last_used_time", System.currentTimeMillis());
        
        return tower;
    }
    
    /**
     * 保存挑战塔到配置节
     */
    public void saveToConfig(ConfigurationSection parent, String key) {
        ConfigurationSection config = parent.createSection(key);
        
        // 基本信息
        config.set("id", id);
        config.set("name", name);
        
        // 区域信息
        ConfigurationSection areaConfig = config.createSection("area");
        areaConfig.set("world", area.getWorld().getName());
        areaConfig.set("min_x", area.getMin().getBlockX());
        areaConfig.set("min_y", area.getMin().getBlockY());
        areaConfig.set("min_z", area.getMin().getBlockZ());
        areaConfig.set("max_x", area.getMax().getBlockX());
        areaConfig.set("max_y", area.getMax().getBlockY());
        areaConfig.set("max_z", area.getMax().getBlockZ());
        
        // 挑战塔配置信息
        ConfigurationSection towerConfig = config.createSection("config");
        this.config.saveToConfig((YamlConfiguration) parent.getRoot(), parent.getCurrentPath() + "." + key + ".config");
        
        // 通天层配置信息
        ConfigurationSection skyTowerConfig = config.createSection("sky_tower");
        this.skyTowerConfig.saveToConfig((YamlConfiguration) parent.getRoot(), parent.getCurrentPath() + "." + key + ".sky_tower");
        
        // 统计信息
        config.set("total_completions", totalCompletions);
        config.set("total_players", totalPlayers);
        config.set("created_time", createdTime);
        config.set("last_used_time", lastUsedTime);
    }
    
    /**
     * 开始挑战
     */
    public boolean startChallenge(java.util.List<org.bukkit.entity.Player> players) {
        if (players == null || players.isEmpty()) {
            return false;
        }
        
        // 更新使用时间
        lastUsedTime = System.currentTimeMillis();
        totalPlayers += players.size();
        
        // 保存到文件
        saveToFile();
        
        // 通过管理器开始实例
        return manager.startTowerInstance(id, players);
    }
    
    /**
     * 挑战完成
     */
    public void onChallengeComplete(boolean success) {
        if (success) {
            totalCompletions++;
        }
        
        // 保存到文件
        saveToFile();
    }
    
    /**
     * 保存到文件
     */
    private void saveToFile() {
        // 通过管理器保存所有数据
        manager.saveAll();
    }
    
    // ========== Getter方法 ==========
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public ChallengeTowerArea getArea() {
        return area;
    }
    
    public ChallengeTowerConfig getConfig() {
        return config;
    }
    
    public SkyTowerConfig getSkyTowerConfig() {
        return skyTowerConfig;
    }
    
    public ChallengeTowerManager getManager() {
        return manager;
    }
    
    public int getTotalCompletions() {
        return totalCompletions;
    }
    
    public int getTotalPlayers() {
        return totalPlayers;
    }
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public long getLastUsedTime() {
        return lastUsedTime;
    }
    
    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }
    
    /**
     * 获取挑战塔信息
     */
    public String getInfo() {
        return String.format(
            "挑战塔: %s (%s)\n" +
            "区域: %s\n" +
            "配置: %s\n" +
            "统计: 完成%d次, 玩家%d人\n" +
            "创建时间: %s",
            name, id,
            area.getBoundaryInfo(),
            config.getConfigSummary(),
            totalCompletions, totalPlayers,
            new java.util.Date(createdTime).toString()
        );
    }
    
    /**
     * 获取简略信息
     */
    public String getShortInfo() {
        return String.format("%s (%s) - 完成: %d次, 玩家: %d人", 
            name, id, totalCompletions, totalPlayers);
    }
    
    /**
     * 检查挑战塔是否可用
     */
    public boolean isAvailable() {
        // 检查是否有活动实例
        ChallengeTowerInstance instance = manager.getActiveInstance(id);
        return instance == null || !instance.isActive();
    }
    
    /**
     * 获取挑战塔状态
     */
    public String getStatus() {
        ChallengeTowerInstance instance = manager.getActiveInstance(id);
        if (instance != null && instance.isActive()) {
            return "进行中 - " + instance.getStatusInfo();
        }
        return "可用";
    }
}