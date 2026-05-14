package io.Sriptirc_wp_1258.upmobs;

import io.Sriptirc_wp_1258.upmobs.challengetower.ChallengeTowerManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Upmobs extends JavaPlugin {
    
    private static Upmobs instance;
    private ConfigManager configManager;
    private MobManager mobManager;
    private EvolutionManager evolutionManager;
    private CommandHandler commandHandler;
    private MobListener mobListener;
    private AttackEffectListener attackEffectListener;
    private ChallengeTowerManager challengeTowerManager;
    private EconomyRewardManager economyRewardManager;
    
    @Override
    public void onEnable() {
        try {
            instance = this;
            getLogger().info("正在初始化升格怪物Up!Mobs插件...");
            
            // 1. 先初始化配置管理器
            configManager = new ConfigManager(this);
            configManager.loadConfig();
            getLogger().info("配置管理器初始化完成");
            
            // 2. 初始化基础管理器
            mobManager = new MobManager(configManager, this);
            evolutionManager = new EvolutionManager(this, configManager, mobManager);
            getLogger().info("基础管理器初始化完成");
            
            // 3. 尝试初始化高级功能（但允许失败）
            try {
                challengeTowerManager = new ChallengeTowerManager(this);
                getLogger().info("挑战塔管理器初始化完成");
            } catch (Exception e) {
                getLogger().warning("挑战塔管理器初始化失败: " + e.getMessage());
                challengeTowerManager = null;
            }
            
            try {
                economyRewardManager = new EconomyRewardManager(this);
                getLogger().info("经济奖励管理器初始化完成");
            } catch (Exception e) {
                getLogger().warning("经济奖励管理器初始化失败: " + e.getMessage());
                economyRewardManager = null;
            }
            
            // 4. 初始化监听器
            try {
                mobListener = new MobListener(this, evolutionManager, mobManager, economyRewardManager);
                attackEffectListener = new AttackEffectListener(this, mobManager);
                
                // 注册事件监听器
                getServer().getPluginManager().registerEvents(mobListener, this);
                getServer().getPluginManager().registerEvents(attackEffectListener, this);
                
                // 启动清理任务
                mobListener.scheduleCleanupTask();
                getLogger().info("事件监听器注册完成");
            } catch (Exception e) {
                getLogger().warning("事件监听器初始化失败: " + e.getMessage());
                mobListener = null;
                attackEffectListener = null;
            }
            
            // 5. 初始化命令处理器
            try {
                commandHandler = new CommandHandler(this, configManager, mobManager, challengeTowerManager);
                setupCommands();
                getLogger().info("命令处理器初始化完成");
            } catch (Exception e) {
                getLogger().warning("命令处理器初始化失败: " + e.getMessage());
                commandHandler = null;
            }
            
            // 6. 输出启动信息
            getLogger().info("========================================");
            getLogger().info("升格怪物Up!Mobs 吸血进化版 + 自然刷新升格");
            getLogger().info("版本: 1.0.0 | Minecraft: 1.20.1");
            getLogger().info("全局增强倍率: " + configManager.getGlobalMultiplier());
            getLogger().info("自然刷新升格: " + (configManager.getConfig().getBoolean("natural_upgrade.enabled", true) ? "启用" : "禁用"));
            getLogger().info("自然刷新几率: " + configManager.getConfig().getDouble("natural_upgrade.spawn_chance", 30.0) + "%");
            getLogger().info("进化条件: " + configManager.getConfig().getInt("evolution.required_attacks", 3) + 
                            "次攻击, " + configManager.getConfig().getDouble("evolution.required_damage", 10.0) + "伤害");
            getLogger().info("最大进化阶段: " + configManager.getConfig().getInt("evolution.max_stages", 3));
            
            if (challengeTowerManager != null) {
                getLogger().info("挑战塔副本系统: 启用");
            } else {
                getLogger().info("挑战塔副本系统: 禁用（初始化失败）");
            }
            
            if (economyRewardManager != null) {
                getLogger().info("经济奖励系统: 启用");
            } else {
                getLogger().info("经济奖励系统: 禁用（初始化失败）");
            }
            
            getLogger().info("========================================");
            getLogger().info("插件初始化完成！");
            
        } catch (Exception e) {
            getLogger().severe("插件初始化过程中发生严重错误: " + e.getMessage());
            e.printStackTrace();
            getLogger().severe("插件将被禁用");
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        // 停止挑战塔管理器
        if (challengeTowerManager != null) {
            challengeTowerManager.stop();
        }
        
        // 保存配置
        configManager.saveConfig();
        
        getLogger().info("升格怪物Up!Mobs 已禁用");
        instance = null;
    }
    

    
    /**
     * 设置指令处理器
     */
    private void setupCommands() {
        // 主命令
        getCommand("upmobs").setExecutor(commandHandler);
        getCommand("upmobs").setTabCompleter(commandHandler);
        
        // 其他命令别名
        getCommand("upmobsreload").setExecutor(commandHandler);
        getCommand("upmobsset").setExecutor(commandHandler);
        getCommand("upmobsget").setExecutor(commandHandler);
        getCommand("upmobspreset").setExecutor(commandHandler);
        getCommand("upmobscustom").setExecutor(commandHandler);
        
        // 挑战塔快捷命令
        getCommand("challengetower").setExecutor(commandHandler);
        getCommand("challengetower").setTabCompleter(commandHandler);
    }
    
    /**
     * 获取插件实例
     */
    public static Upmobs getInstance() {
        return instance;
    }
    
    /**
     * 获取配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * 获取生物管理器
     */
    public MobManager getMobManager() {
        return mobManager;
    }
    
    /**
     * 获取进化管理器
     */
    public EvolutionManager getEvolutionManager() {
        return evolutionManager;
    }
    
    /**
     * 获取经济奖励管理器
     */
    public EconomyRewardManager getEconomyRewardManager() {
        return economyRewardManager;
    }
    
    /**
     * 获取挑战塔管理器
     */
    public ChallengeTowerManager getChallengeTowerManager() {
        return challengeTowerManager;
    }
}
