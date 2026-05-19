package io.Sriptirc_wp_1258.gost;

import io.Sriptirc_wp_1258.gost.commands.GostCommand;
import io.Sriptirc_wp_1258.gost.commands.GostAdminCommand;
import io.Sriptirc_wp_1258.gost.commands.DivineGuardianCommand;
import io.Sriptirc_wp_1258.gost.commands.GhostParticleCommand;
import io.Sriptirc_wp_1258.gost.listeners.*;
import io.Sriptirc_wp_1258.gost.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class Gost extends JavaPlugin {
    
    private static Gost instance;
    
    // 管理器
    private ConfigManager configManager;
    private GameManager gameManager;
    private PlayerManager playerManager;
    private ItemManager itemManager;
    private TeamManager teamManager;
    private AreaManager areaManager;
    private SelectionManager selectionManager;
    private EconomyManager economyManager;
    private ActionBarManager actionBarManager;
    private LanguageManager languageManager;
    private ItemSpawnManager itemSpawnManager;
    private SecondChanceListener secondChanceListener;
    private DarkEffectManager darkEffectManager;
    private HeartbeatManager heartbeatManager;
    private DivineGuardianManager divineGuardianManager;
    private GhostParticleManager ghostParticleManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        try {
            // 初始化管理器
            configManager = new ConfigManager(this);
            economyManager = new EconomyManager(this);
            areaManager = new AreaManager(this);
            selectionManager = new SelectionManager(this);
            teamManager = new TeamManager(this);
            itemManager = new ItemManager(this);
            playerManager = new PlayerManager(this);
            gameManager = new GameManager(this);
            actionBarManager = new ActionBarManager(this);
            
            // 尝试初始化 LanguageManager，如果失败则继续
            try {
                languageManager = new LanguageManager(this);
            } catch (NoClassDefFoundError | Exception e) {
                getLogger().warning("无法初始化 LanguageManager: " + e.getMessage());
                getLogger().warning("语言功能将不可用，但插件会继续运行");
                languageManager = null;
            }
            
            itemSpawnManager = new ItemSpawnManager(this);
            secondChanceListener = new SecondChanceListener(this);
            darkEffectManager = new DarkEffectManager(this);
            heartbeatManager = new HeartbeatManager(this);
            divineGuardianManager = new DivineGuardianManager(this);
            ghostParticleManager = new GhostParticleManager(this);
        } catch (Exception e) {
            getLogger().severe("初始化管理器时发生错误: " + e.getMessage());
            e.printStackTrace();
            // 设置游戏管理器为null，避免onDisable时出现NPE
            gameManager = null;
            return;
        }
        
        try {
            // 加载语言
            if (languageManager != null) {
                languageManager.reload();
            }
            
            // 加载神圣守护配置
            divineGuardianManager.loadConfig();
            
            // 加载鬼玩家粒子效果配置
            ghostParticleManager.loadConfig();
            
            // 加载黑暗效果配置
            darkEffectManager.loadConfig();
            
            // 插件加载完成提示
            getLogger().info("==========================================");
            
            // 注册命令
            getCommand("gost").setExecutor(new GostCommand(this));
            getCommand("gostadmin").setExecutor(new GostAdminCommand(this));
            getCommand("divineguardian").setExecutor(new DivineGuardianCommand(this));
            getCommand("divineguardian").setTabCompleter(new DivineGuardianCommand(this));
            getCommand("ghostparticle").setExecutor(new GhostParticleCommand(this));
            getCommand("ghostparticle").setTabCompleter(new GhostParticleCommand(this));
            
            // 注册监听器
            getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
            getServer().getPluginManager().registerEvents(new GameListener(this), this);
            getServer().getPluginManager().registerEvents(new InfectionListener(this), this);
            getServer().getPluginManager().registerEvents(new ItemListener(this), this);
            getServer().getPluginManager().registerEvents(new SelectionListener(this), this);
            getServer().getPluginManager().registerEvents(secondChanceListener, this);
            getServer().getPluginManager().registerEvents(new HolyRedemptionListener(this), this);
            getServer().getPluginManager().registerEvents(new DemonHunterPhaseListener(this), this);
            
            getLogger().info("==========================================");
            getLogger().info("作者: 来自太空的小头脑");
            getLogger().info("主页: https://space.bilibili.com/3493116665400113");
            getLogger().info("==========================================");
        } catch (Exception e) {
            getLogger().severe("注册命令和监听器时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDisable() {
        if (gameManager != null && gameManager.isGameRunning()) {
            gameManager.forceStopGame();
        }
        
        // 清理队伍
        if (teamManager != null) {
            teamManager.cleanup();
        }
        
        // 清理神圣守护数据
        if (divineGuardianManager != null) {
            divineGuardianManager.cleanup();
        }
        
        // 清理鬼玩家粒子效果数据
        if (ghostParticleManager != null) {
            ghostParticleManager.cleanup();
        }
        
        getLogger().info("Gost 插件已禁用");
    }
    
    // 获取管理器实例
    public static Gost getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public GameManager getGameManager() {
        return gameManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public ItemManager getItemManager() {
        return itemManager;
    }
    
    public TeamManager getTeamManager() {
        return teamManager;
    }
    
    public AreaManager getAreaManager() {
        return areaManager;
    }
    
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public ActionBarManager getActionBarManager() {
        return actionBarManager;
    }
    

    
    public LanguageManager getLanguageManager() {
        if (languageManager == null) {
            // 直接创建LanguageManager，它已经内置了默认消息和友好回退
            languageManager = new LanguageManager(this);
        }
        return languageManager;
    }
    
    public ItemSpawnManager getItemSpawnManager() {
        return itemSpawnManager;
    }
    
    public SecondChanceListener getSecondChanceListener() {
        return secondChanceListener;
    }
    
    public DarkEffectManager getDarkEffectManager() {
        return darkEffectManager;
    }
    
    public HeartbeatManager getHeartbeatManager() {
        return heartbeatManager;
    }
    

    
    public DivineGuardianManager getDivineGuardianManager() {
        return divineGuardianManager;
    }
    
    public GhostParticleManager getGhostParticleManager() {
        return ghostParticleManager;
    }
    

    

    

}