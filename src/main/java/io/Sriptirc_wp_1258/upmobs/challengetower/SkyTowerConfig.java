package io.Sriptirc_wp_1258.upmobs.challengetower;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * 通天层配置
 * 存储通天层的各种参数和设置
 */
public class SkyTowerConfig {
    
    // 基础配置
    private boolean enabled = true;                     // 是否启用通天层
    private String unlockRequirement = "complete_tower"; // 解锁要求：complete_tower=通关挑战塔
    
    // 怪物配置
    private int baseMonsterCount = 10;                  // 基础怪物数量
    private List<String> monsterTypes = new ArrayList<>(); // 怪物类型
    private boolean refreshOnDeath = true;              // 死亡后刷新怪物
    private int refreshDelaySeconds = 5;                // 刷新延迟（秒）
    
    // 时间限制
    private int timeLimitMinutes = 10;                  // 时间限制（分钟）
    private boolean showTimer = true;                   // 显示倒计时
    private int warningTimeSeconds = 60;                // 警告时间（秒）
    
    // 难度配置
    private double difficultyIncreasePerMinute = 0.1;   // 每分钟难度增加
    private double maxDifficultyMultiplier = 3.0;       // 最大难度倍率
    
    // 阶段递进配置（分钟）
    private int stage1StartMinute = 0;                  // 第一阶段开始（普通升格怪物）
    private int stage2StartMinute = 2;                  // 第二阶段开始（进化怪物）
    private int stage3StartMinute = 5;                  // 第三阶段开始（高阶段进化怪物）
    private int stage4StartMinute = 8;                  // 第四阶段开始（第四阶段怪物）
    
    // 奖励配置
    private double baseRewardPerKill = 50.0;            // 基础击杀奖励
    private double rewardMultiplierPerMinute = 1.2;     // 每分钟奖励倍率
    private int milestoneIntervalMinutes = 5;           // 里程碑间隔（分钟）
    private double milestoneBonus = 100.0;              // 里程碑额外奖励
    
    // 排行榜配置
    private boolean enableLeaderboard = true;           // 启用排行榜
    private int leaderboardSize = 10;                   // 排行榜大小
    private boolean savePlayerStats = true;             // 保存玩家统计
    
    // 特殊效果
    private boolean enablePlayerStatusDisplay = true;   // 启用玩家状态显示
    private int statusUpdateIntervalSeconds = 10;       // 状态更新间隔
    
    public SkyTowerConfig() {
        // 初始化默认怪物类型（第四阶段怪物）
        initializeDefaultMonsterTypes();
    }
    
    /**
     * 初始化默认怪物类型
     */
    private void initializeDefaultMonsterTypes() {
        // 第四阶段怪物类型
        monsterTypes.add("STAGE4_ZOMBIE");
        monsterTypes.add("STAGE4_SKELETON");
        monsterTypes.add("STAGE4_CREEPER");
        monsterTypes.add("STAGE4_SPIDER");
        monsterTypes.add("STAGE4_ENDERMAN");
    }
    
    /**
     * 从配置节加载配置
     */
    public void loadFromConfig(ConfigurationSection config) {
        if (config == null) return;
        
        enabled = config.getBoolean("enabled", enabled);
        unlockRequirement = config.getString("unlock_requirement", unlockRequirement);
        
        // 怪物配置
        baseMonsterCount = config.getInt("monster.base_count", baseMonsterCount);
        refreshOnDeath = config.getBoolean("monster.refresh_on_death", refreshOnDeath);
        refreshDelaySeconds = config.getInt("monster.refresh_delay_seconds", refreshDelaySeconds);
        
        // 加载怪物类型
        if (config.contains("monster.types")) {
            monsterTypes.clear();
            monsterTypes.addAll(config.getStringList("monster.types"));
        }
        
        // 如果怪物类型为空，使用默认的
        if (monsterTypes.isEmpty()) {
            initializeDefaultMonsterTypes();
        }
        
        // 时间限制
        timeLimitMinutes = config.getInt("time_limit.minutes", timeLimitMinutes);
        showTimer = config.getBoolean("time_limit.show_timer", showTimer);
        warningTimeSeconds = config.getInt("time_limit.warning_seconds", warningTimeSeconds);
        
        // 难度配置
        difficultyIncreasePerMinute = config.getDouble("difficulty.increase_per_minute", difficultyIncreasePerMinute);
        maxDifficultyMultiplier = config.getDouble("difficulty.max_multiplier", maxDifficultyMultiplier);
        
        // 阶段递进配置
        stage1StartMinute = config.getInt("stages.stage1_start_minute", stage1StartMinute);
        stage2StartMinute = config.getInt("stages.stage2_start_minute", stage2StartMinute);
        stage3StartMinute = config.getInt("stages.stage3_start_minute", stage3StartMinute);
        stage4StartMinute = config.getInt("stages.stage4_start_minute", stage4StartMinute);
        
        // 奖励配置
        baseRewardPerKill = config.getDouble("rewards.base_per_kill", baseRewardPerKill);
        rewardMultiplierPerMinute = config.getDouble("rewards.multiplier_per_minute", rewardMultiplierPerMinute);
        milestoneIntervalMinutes = config.getInt("rewards.milestone_interval_minutes", milestoneIntervalMinutes);
        milestoneBonus = config.getDouble("rewards.milestone_bonus", milestoneBonus);
        
        // 排行榜配置
        enableLeaderboard = config.getBoolean("leaderboard.enabled", enableLeaderboard);
        leaderboardSize = config.getInt("leaderboard.size", leaderboardSize);
        savePlayerStats = config.getBoolean("leaderboard.save_stats", savePlayerStats);
        
        // 特殊效果
        enablePlayerStatusDisplay = config.getBoolean("effects.player_status_display", enablePlayerStatusDisplay);
        statusUpdateIntervalSeconds = config.getInt("effects.status_update_seconds", statusUpdateIntervalSeconds);
    }
    
    /**
     * 保存配置到配置节
     */
    public void saveToConfig(YamlConfiguration config, String path) {
        config.set(path + ".enabled", enabled);
        config.set(path + ".unlock_requirement", unlockRequirement);
        
        // 怪物配置
        config.set(path + ".monster.base_count", baseMonsterCount);
        config.set(path + ".monster.refresh_on_death", refreshOnDeath);
        config.set(path + ".monster.refresh_delay_seconds", refreshDelaySeconds);
        config.set(path + ".monster.types", monsterTypes);
        
        // 时间限制
        config.set(path + ".time_limit.minutes", timeLimitMinutes);
        config.set(path + ".time_limit.show_timer", showTimer);
        config.set(path + ".time_limit.warning_seconds", warningTimeSeconds);
        
        // 难度配置
        config.set(path + ".difficulty.increase_per_minute", difficultyIncreasePerMinute);
        config.set(path + ".difficulty.max_multiplier", maxDifficultyMultiplier);
        
        // 奖励配置
        config.set(path + ".rewards.base_per_kill", baseRewardPerKill);
        config.set(path + ".rewards.multiplier_per_minute", rewardMultiplierPerMinute);
        config.set(path + ".rewards.milestone_interval_minutes", milestoneIntervalMinutes);
        config.set(path + ".rewards.milestone_bonus", milestoneBonus);
        
        // 排行榜配置
        config.set(path + ".leaderboard.enabled", enableLeaderboard);
        config.set(path + ".leaderboard.size", leaderboardSize);
        config.set(path + ".leaderboard.save_stats", savePlayerStats);
        
        // 特殊效果
        config.set(path + ".effects.player_status_display", enablePlayerStatusDisplay);
        config.set(path + ".effects.status_update_seconds", statusUpdateIntervalSeconds);
    }
    
    // ========== Getter方法 ==========
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getUnlockRequirement() {
        return unlockRequirement;
    }
    
    public void setUnlockRequirement(String unlockRequirement) {
        this.unlockRequirement = unlockRequirement;
    }
    
    public int getBaseMonsterCount() {
        return baseMonsterCount;
    }
    
    public void setBaseMonsterCount(int baseMonsterCount) {
        this.baseMonsterCount = baseMonsterCount;
    }
    
    public List<String> getMonsterTypes() {
        return new ArrayList<>(monsterTypes);
    }
    
    public void setMonsterTypes(List<String> monsterTypes) {
        this.monsterTypes = new ArrayList<>(monsterTypes);
    }
    
    public boolean isRefreshOnDeath() {
        return refreshOnDeath;
    }
    
    public void setRefreshOnDeath(boolean refreshOnDeath) {
        this.refreshOnDeath = refreshOnDeath;
    }
    
    public int getRefreshDelaySeconds() {
        return refreshDelaySeconds;
    }
    
    public void setRefreshDelaySeconds(int refreshDelaySeconds) {
        this.refreshDelaySeconds = refreshDelaySeconds;
    }
    
    public int getTimeLimitMinutes() {
        return timeLimitMinutes;
    }
    
    public void setTimeLimitMinutes(int timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }
    
    public boolean isShowTimer() {
        return showTimer;
    }
    
    public void setShowTimer(boolean showTimer) {
        this.showTimer = showTimer;
    }
    
    public int getWarningTimeSeconds() {
        return warningTimeSeconds;
    }
    
    public void setWarningTimeSeconds(int warningTimeSeconds) {
        this.warningTimeSeconds = warningTimeSeconds;
    }
    
    public double getDifficultyIncreasePerMinute() {
        return difficultyIncreasePerMinute;
    }
    
    public void setDifficultyIncreasePerMinute(double difficultyIncreasePerMinute) {
        this.difficultyIncreasePerMinute = difficultyIncreasePerMinute;
    }
    
    public double getMaxDifficultyMultiplier() {
        return maxDifficultyMultiplier;
    }
    
    public void setMaxDifficultyMultiplier(double maxDifficultyMultiplier) {
        this.maxDifficultyMultiplier = maxDifficultyMultiplier;
    }
    
    // ========== 阶段递配置 Getter ==========
    
    public int getStage1StartMinute() {
        return stage1StartMinute;
    }
    
    public int getStage2StartMinute() {
        return stage2StartMinute;
    }
    
    public int getStage3StartMinute() {
        return stage3StartMinute;
    }
    
    public int getStage4StartMinute() {
        return stage4StartMinute;
    }
    
    /**
     * 根据已过分钟数获取当前阶段
     */
    public int getCurrentStage(int elapsedMinutes) {
        if (elapsedMinutes >= stage4StartMinute) return 4;
        if (elapsedMinutes >= stage3StartMinute) return 3;
        if (elapsedMinutes >= stage2StartMinute) return 2;
        return 1;
    }
    
    public double getBaseRewardPerKill() {
        return baseRewardPerKill;
    }
    
    public void setBaseRewardPerKill(double baseRewardPerKill) {
        this.baseRewardPerKill = baseRewardPerKill;
    }
    
    public double getRewardMultiplierPerMinute() {
        return rewardMultiplierPerMinute;
    }
    
    public void setRewardMultiplierPerMinute(double rewardMultiplierPerMinute) {
        this.rewardMultiplierPerMinute = rewardMultiplierPerMinute;
    }
    
    public int getMilestoneIntervalMinutes() {
        return milestoneIntervalMinutes;
    }
    
    public void setMilestoneIntervalMinutes(int milestoneIntervalMinutes) {
        this.milestoneIntervalMinutes = milestoneIntervalMinutes;
    }
    
    public double getMilestoneBonus() {
        return milestoneBonus;
    }
    
    public void setMilestoneBonus(double milestoneBonus) {
        this.milestoneBonus = milestoneBonus;
    }
    
    public boolean isEnableLeaderboard() {
        return enableLeaderboard;
    }
    
    public void setEnableLeaderboard(boolean enableLeaderboard) {
        this.enableLeaderboard = enableLeaderboard;
    }
    
    public int getLeaderboardSize() {
        return leaderboardSize;
    }
    
    public void setLeaderboardSize(int leaderboardSize) {
        this.leaderboardSize = leaderboardSize;
    }
    
    public boolean isSavePlayerStats() {
        return savePlayerStats;
    }
    
    public void setSavePlayerStats(boolean savePlayerStats) {
        this.savePlayerStats = savePlayerStats;
    }
    
    public boolean isEnablePlayerStatusDisplay() {
        return enablePlayerStatusDisplay;
    }
    
    public void setEnablePlayerStatusDisplay(boolean enablePlayerStatusDisplay) {
        this.enablePlayerStatusDisplay = enablePlayerStatusDisplay;
    }
    
    public int getStatusUpdateIntervalSeconds() {
        return statusUpdateIntervalSeconds;
    }
    
    public void setStatusUpdateIntervalSeconds(int statusUpdateIntervalSeconds) {
        this.statusUpdateIntervalSeconds = statusUpdateIntervalSeconds;
    }
    
    /**
     * 获取指定时间的难度倍率
     */
    public double getDifficultyMultiplierForTime(int elapsedMinutes) {
        double multiplier = 1.0 + (difficultyIncreasePerMinute * elapsedMinutes);
        return Math.min(multiplier, maxDifficultyMultiplier);
    }
    
    /**
     * 获取指定时间的奖励倍率
     */
    public double getRewardMultiplierForTime(int elapsedMinutes) {
        return Math.pow(rewardMultiplierPerMinute, elapsedMinutes);
    }
    
    /**
     * 获取配置摘要
     */
    public String getConfigSummary() {
        return String.format(
            "通天层配置: 启用=%s, 解锁要求=%s\n" +
            "怪物配置: 数量=%d, 刷新=%s, 延迟=%d秒\n" +
            "时间限制: %d分钟, 显示计时器=%s\n" +
            "难度配置: 每分钟增加=%.2f, 最大倍率=%.1f\n" +
            "奖励配置: 基础奖励=%.1f, 里程碑间隔=%d分钟, 里程碑奖励=%.1f\n" +
            "排行榜: 启用=%s, 大小=%d, 保存统计=%s",
            enabled, unlockRequirement,
            baseMonsterCount, refreshOnDeath, refreshDelaySeconds,
            timeLimitMinutes, showTimer,
            difficultyIncreasePerMinute, maxDifficultyMultiplier,
            baseRewardPerKill, milestoneIntervalMinutes, milestoneBonus,
            enableLeaderboard, leaderboardSize, savePlayerStats
        );
    }
}