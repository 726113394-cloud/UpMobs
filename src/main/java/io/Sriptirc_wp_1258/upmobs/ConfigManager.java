package io.Sriptirc_wp_1258.upmobs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 配置管理器
 * 负责加载、保存和管理所有生物配置
 */
public class ConfigManager {
    
    private final JavaPlugin plugin;
    private final Map<String, MobAttributes> mobAttributes;  // key: entityType.name() 或 customName
    private final Map<String, MobAttributes> customMobs;      // 自定义生物
    private final Map<String, Map<String, MobAttributes>> presets;  // 预设配置
    
    private boolean randomIndividualUpgrade = true;
    private double globalMultiplier = 5.0;  // 默认5倍增强
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.mobAttributes = new HashMap<>();
        this.customMobs = new HashMap<>();
        this.presets = new HashMap<>();
    }
    
    /**
     * 加载配置
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        
        // 加载全局设置
        randomIndividualUpgrade = plugin.getConfig().getBoolean("random_individual_upgrade", true);
        globalMultiplier = plugin.getConfig().getDouble("global_multiplier", 5.0);
        
        // 清空现有配置
        mobAttributes.clear();
        customMobs.clear();
        presets.clear();
        
        // 加载原版生物配置
        ConfigurationSection mobsSection = plugin.getConfig().getConfigurationSection("mobs");
        if (mobsSection != null) {
            for (String key : mobsSection.getKeys(false)) {
                try {
                    EntityType entityType = EntityType.valueOf(key.toUpperCase());
                    ConfigurationSection mobConfig = mobsSection.getConfigurationSection(key);
                    
                    if (mobConfig != null) {
                        Map<String, Object> map = mobConfig.getValues(false);
                        MobAttributes attributes = MobAttributes.fromMap(map);
                        mobAttributes.put(key.toUpperCase(), attributes);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("未知的生物类型: " + key);
                }
            }
        }
        
        // 加载自定义生物
        ConfigurationSection customSection = plugin.getConfig().getConfigurationSection("custom_mobs");
        if (customSection != null) {
            for (String name : customSection.getKeys(false)) {
                ConfigurationSection mobConfig = customSection.getConfigurationSection(name);
                if (mobConfig != null) {
                    Map<String, Object> map = mobConfig.getValues(false);
                    MobAttributes attributes = MobAttributes.fromMap(map);
                    customMobs.put(name, attributes);
                }
            }
        }
        
        // 加载预设
        ConfigurationSection presetsSection = plugin.getConfig().getConfigurationSection("presets");
        if (presetsSection != null) {
            for (String presetName : presetsSection.getKeys(false)) {
                ConfigurationSection presetSection = presetsSection.getConfigurationSection(presetName);
                if (presetSection != null) {
                    Map<String, MobAttributes> preset = new HashMap<>();
                    
                    ConfigurationSection presetMobs = presetSection.getConfigurationSection("mobs");
                    if (presetMobs != null) {
                        for (String key : presetMobs.getKeys(false)) {
                            ConfigurationSection mobConfig = presetMobs.getConfigurationSection(key);
                            if (mobConfig != null) {
                                Map<String, Object> map = mobConfig.getValues(false);
                                MobAttributes attributes = MobAttributes.fromMap(map);
                                preset.put(key, attributes);
                            }
                        }
                    }
                    
                    presets.put(presetName, preset);
                }
            }
        }
        
        // 如果没有配置，创建默认配置
        if (mobAttributes.isEmpty()) {
            createDefaultConfig();
        }
        
        plugin.getLogger().info("已加载 " + mobAttributes.size() + " 种原版生物配置");
        plugin.getLogger().info("已加载 " + customMobs.size() + " 种自定义生物配置");
        plugin.getLogger().info("已加载 " + presets.size() + " 个预设配置");
    }
    
    /**
     * 创建默认配置（5倍增强）
     */
    private void createDefaultConfig() {
        plugin.getLogger().info("创建默认生物配置（5倍增强）...");
        
        // 为所有原版生物创建默认配置
        for (EntityType entityType : EntityType.values()) {
            if (entityType.isAlive() && entityType.isSpawnable()) {
                MobAttributes attributes = new MobAttributes(entityType);
                
                // 应用5倍增强
                attributes.setHealth(AttributeValue.percent(500));  // 5倍血量
                attributes.setSpeed(AttributeValue.percent(200));   // 2倍速度
                attributes.setArmor(AttributeValue.percent(500));   // 5倍护甲
                attributes.setDamage(AttributeValue.percent(500));  // 5倍伤害
                attributes.setKnockbackResistance(AttributeValue.percent(300));
                attributes.setFollowRange(AttributeValue.percent(300));
                attributes.setAttackSpeed(AttributeValue.percent(200));
                
                // 为某些生物添加特殊效果
                if (entityType == EntityType.BLAZE || entityType == EntityType.MAGMA_CUBE) {
                    attributes.setFireResistant(true);
                }
                
                mobAttributes.put(entityType.name(), attributes);
            }
        }
        
        // 保存到配置文件
        saveConfig();
    }
    
    /**
     * 保存配置
     */
    public void saveConfig() {
        YamlConfiguration config = new YamlConfiguration();
        
        // 保存全局设置
        config.set("ScriptIrc-config-version", 1);
        config.set("random_individual_upgrade", randomIndividualUpgrade);
        config.set("global_multiplier", globalMultiplier);
        
        // 保存原版生物配置
        ConfigurationSection mobsSection = config.createSection("mobs");
        for (Map.Entry<String, MobAttributes> entry : mobAttributes.entrySet()) {
            mobsSection.set(entry.getKey().toLowerCase(), entry.getValue().toMap());
        }
        
        // 保存自定义生物
        ConfigurationSection customSection = config.createSection("custom_mobs");
        for (Map.Entry<String, MobAttributes> entry : customMobs.entrySet()) {
            customSection.set(entry.getKey(), entry.getValue().toMap());
        }
        
        // 保存预设
        ConfigurationSection presetsSection = config.createSection("presets");
        for (Map.Entry<String, Map<String, MobAttributes>> entry : presets.entrySet()) {
            ConfigurationSection presetSection = presetsSection.createSection(entry.getKey());
            ConfigurationSection presetMobs = presetSection.createSection("mobs");
            
            for (Map.Entry<String, MobAttributes> mobEntry : entry.getValue().entrySet()) {
                presetMobs.set(mobEntry.getKey(), mobEntry.getValue().toMap());
            }
        }
        
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("保存配置文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取生物属性配置
     */
    public MobAttributes getMobAttributes(EntityType entityType) {
        return mobAttributes.get(entityType.name());
    }
    
    /**
     * 获取自定义生物属性配置
     */
    public MobAttributes getCustomMobAttributes(String name) {
        return customMobs.get(name);
    }
    
    /**
     * 获取所有原版生物配置
     */
    public Map<String, MobAttributes> getAllMobAttributes() {
        return new HashMap<>(mobAttributes);
    }
    
    /**
     * 获取所有自定义生物配置
     */
    public Map<String, MobAttributes> getAllCustomMobs() {
        return new HashMap<>(customMobs);
    }
    
    /**
     * 设置生物属性配置
     */
    public void setMobAttributes(EntityType entityType, MobAttributes attributes) {
        mobAttributes.put(entityType.name(), attributes);
        saveConfig();
    }
    
    /**
     * 添加或更新自定义生物
     */
    public void setCustomMob(String name, MobAttributes attributes) {
        customMobs.put(name, attributes);
        saveConfig();
    }
    
    /**
     * 删除自定义生物
     */
    public boolean removeCustomMob(String name) {
        boolean removed = customMobs.remove(name) != null;
        if (removed) {
            saveConfig();
        }
        return removed;
    }
    
    /**
     * 保存预设
     */
    public void savePreset(String name, Map<String, MobAttributes> preset) {
        presets.put(name, new HashMap<>(preset));
        saveConfig();
    }
    
    /**
     * 加载预设
     */
    public Map<String, MobAttributes> loadPreset(String name) {
        return presets.get(name);
    }
    
    /**
     * 删除预设
     */
    public boolean removePreset(String name) {
        boolean removed = presets.remove(name) != null;
        if (removed) {
            saveConfig();
        }
        return removed;
    }
    
    /**
     * 获取所有预设名称
     */
    public Set<String> getPresetNames() {
        return presets.keySet();
    }
    
    /**
     * 是否启用随机个体升级
     */
    public boolean isRandomIndividualUpgrade() {
        return randomIndividualUpgrade;
    }
    
    /**
     * 设置随机个体升级
     */
    public void setRandomIndividualUpgrade(boolean enabled) {
        this.randomIndividualUpgrade = enabled;
        saveConfig();
    }
    
    /**
     * 获取全局倍率
     */
    public double getGlobalMultiplier() {
        return globalMultiplier;
    }
    
    /**
     * 设置全局倍率
     */
    public void setGlobalMultiplier(double multiplier) {
        this.globalMultiplier = multiplier;
        saveConfig();
    }
    
    /**
     * 获取Bukkit配置对象
     */
    public org.bukkit.configuration.file.YamlConfiguration getConfig() {
        return (org.bukkit.configuration.file.YamlConfiguration) plugin.getConfig();
    }
    
    /**
     * 导出配置到文件
     */
    public boolean exportConfig(String fileName) {
        try {
            File exportFile = new File(plugin.getDataFolder(), fileName);
            YamlConfiguration config = new YamlConfiguration();
            
            // 导出所有配置
            config.set("mobs", mobAttributes);
            config.set("custom_mobs", customMobs);
            config.set("presets", presets);
            
            config.save(exportFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("导出配置失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 从文件导入配置
     */
    public boolean importConfig(String fileName) {
        try {
            File importFile = new File(plugin.getDataFolder(), fileName);
            if (!importFile.exists()) {
                return false;
            }
            
            YamlConfiguration config = YamlConfiguration.loadConfiguration(importFile);
            
            // 清空现有配置
            mobAttributes.clear();
            customMobs.clear();
            presets.clear();
            
            // 导入配置（这里简化处理，实际需要更复杂的解析）
            // TODO: 实现完整的导入逻辑
            
            saveConfig();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("导入配置失败: " + e.getMessage());
            return false;
        }
    }
}