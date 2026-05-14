package io.Sriptirc_wp_1258.upmobs.challengetower;

import io.Sriptirc_wp_1258.upmobs.Upmobs;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 挑战塔管理器
 * 管理所有挑战塔的创建、运行和删除
 */
public class ChallengeTowerManager implements Listener {
    
    private final Upmobs plugin;
    private final File towersFile;
    private final File progressFile;
    
    // 数据存储
    private final Map<String, ChallengeTower> towers = new ConcurrentHashMap<>();
    private final Map<String, ChallengeTowerInstance> activeInstances = new ConcurrentHashMap<>();
    private final Map<UUID, SkyTowerInstance> activeSkyTowerInstances = new ConcurrentHashMap<>();
    private final Map<UUID, ChallengeTowerPlayerProgress> playerProgress = new ConcurrentHashMap<>();
    
    // 组件
    private ChallengeTowerQueue queue;
    private AreaSelectionTool selectionTool;
    
    // 配置
    private ChallengeTowerConfig defaultConfig;
    
    public ChallengeTowerManager(Upmobs plugin) {
        this.plugin = plugin;
        this.towersFile = new File(plugin.getDataFolder(), "challenge_towers.yml");
        this.progressFile = new File(plugin.getDataFolder(), "challenge_tower_progress.yml");
        
        // 初始化组件
        this.defaultConfig = new ChallengeTowerConfig();
        this.queue = new ChallengeTowerQueue(this);
        this.selectionTool = new AreaSelectionTool(plugin);
        
        // 加载数据
        loadTowers();
        loadPlayerProgress();
        
        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getPluginManager().registerEvents(selectionTool, plugin);
    }
    
    /**
     * 保存所有数据
     */
    public void saveAll() {
        saveTowers();
        savePlayerProgress();
        plugin.getLogger().info("挑战塔数据已保存");
    }
    
    /**
     * 加载挑战塔数据
     */
    private void loadTowers() {
        if (!towersFile.exists()) {
            plugin.getLogger().info("挑战塔数据文件不存在，跳过加载");
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(towersFile);
        
        if (config.contains("towers")) {
            ConfigurationSection towersSection = config.getConfigurationSection("towers");
            if (towersSection != null) {
                for (String towerId : towersSection.getKeys(false)) {
                    try {
                        ChallengeTower tower = ChallengeTower.fromConfig(towersSection.getConfigurationSection(towerId), this);
                        if (tower != null) {
                            towers.put(towerId, tower);
                            plugin.getLogger().info("加载挑战塔: " + tower.getName() + " (" + towerId + ")");
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("加载挑战塔失败: " + towerId + " - " + e.getMessage());
                    }
                }
            }
        }
        
        plugin.getLogger().info("已加载 " + towers.size() + " 个挑战塔");
    }
    
    /**
     * 保存挑战塔数据
     */
    private void saveTowers() {
        YamlConfiguration config = new YamlConfiguration();
        
        ConfigurationSection towersSection = config.createSection("towers");
        for (Map.Entry<String, ChallengeTower> entry : towers.entrySet()) {
            entry.getValue().saveToConfig(towersSection, entry.getKey());
        }
        
        try {
            config.save(towersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存挑战塔数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 加载玩家进度数据
     */
    private void loadPlayerProgress() {
        if (!progressFile.exists()) {
            plugin.getLogger().info("玩家进度文件不存在，跳过加载");
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(progressFile);
        
        if (config.contains("progress")) {
            ConfigurationSection progressSection = config.getConfigurationSection("progress");
            if (progressSection != null) {
                for (String playerIdStr : progressSection.getKeys(false)) {
                    try {
                        UUID playerId = UUID.fromString(playerIdStr);
                        ConfigurationSection playerSection = progressSection.getConfigurationSection(playerIdStr);
                        
                        if (playerSection != null) {
                            ChallengeTowerPlayerProgress progress = new ChallengeTowerPlayerProgress(
                                playerId,
                                playerSection.getString("player_name", "未知玩家")
                            );
                            
                            Map<String, Object> data = new HashMap<>();
                            for (String key : playerSection.getKeys(false)) {
                                data.put(key, playerSection.get(key));
                            }
                            
                            progress.loadFromData(data);
                            playerProgress.put(playerId, progress);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("加载玩家进度失败: " + playerIdStr + " - " + e.getMessage());
                    }
                }
            }
        }
        
        plugin.getLogger().info("已加载 " + playerProgress.size() + " 个玩家进度");
    }
    
    /**
     * 保存玩家进度数据
     */
    private void savePlayerProgress() {
        YamlConfiguration config = new YamlConfiguration();
        
        ConfigurationSection progressSection = config.createSection("progress");
        for (Map.Entry<UUID, ChallengeTowerPlayerProgress> entry : playerProgress.entrySet()) {
            progressSection.set(entry.getKey().toString(), entry.getValue().toData());
        }
        
        try {
            config.save(progressFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存玩家进度失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建新的挑战塔
     */
    public boolean createTower(Player creator, String towerId, String towerName, ChallengeTowerArea area) {
        // 检查ID是否已存在
        if (towers.containsKey(towerId)) {
            creator.sendMessage(ChatColor.RED + "挑战塔ID已存在: " + towerId);
            return false;
        }
        
        // 检查区域是否有效
        if (!area.isValidForChallengeTower()) {
            creator.sendMessage(ChatColor.RED + "区域太小，最小要求: 10x10x5");
            creator.sendMessage(ChatColor.YELLOW + "当前区域: " + area.getBoundaryInfo());
            return false;
        }
        
        // 检查区域是否与其他塔重叠
        for (ChallengeTower existingTower : towers.values()) {
            if (area.overlapsWith(existingTower.getArea())) {
                creator.sendMessage(ChatColor.RED + "区域与现有挑战塔重叠: " + existingTower.getName());
                return false;
            }
        }
        
        // 创建挑战塔
        SkyTowerConfig skyTowerConfig = new SkyTowerConfig();
        // 从config.yml加载通天层配置
        if (plugin.getConfig().contains("sky_tower")) {
            skyTowerConfig.loadFromConfig(plugin.getConfig().getConfigurationSection("sky_tower"));
        }
        ChallengeTower tower = new ChallengeTower(towerId, towerName, area, defaultConfig, skyTowerConfig, this);
        towers.put(towerId, tower);
        
        // 保存到文件
        saveTowers();
        
        // 发送成功消息
        creator.sendMessage(ChatColor.GREEN + "§l✓ 挑战塔创建成功！");
        creator.sendMessage(ChatColor.YELLOW + "名称: " + ChatColor.WHITE + towerName);
        creator.sendMessage(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + towerId);
        creator.sendMessage(ChatColor.YELLOW + "区域: " + ChatColor.WHITE + area.getBoundaryInfo());
        creator.sendMessage(ChatColor.YELLOW + "使用指令加入队列: " + ChatColor.WHITE + "/upmobs tower join " + towerId);
        
        return true;
    }
    
    /**
     * 删除挑战塔
     */
    public boolean deleteTower(Player deleter, String towerId) {
        if (!towers.containsKey(towerId)) {
            deleter.sendMessage(ChatColor.RED + "挑战塔不存在: " + towerId);
            return false;
        }
        
        // 检查是否有活动实例
        if (activeInstances.containsKey(towerId)) {
            deleter.sendMessage(ChatColor.RED + "挑战塔正在进行中，无法删除");
            return false;
        }
        
        // 从队列中移除
        queue.cleanupEmptyQueues();
        
        // 删除挑战塔
        ChallengeTower removed = towers.remove(towerId);
        
        // 保存到文件
        saveTowers();
        
        deleter.sendMessage(ChatColor.GREEN + "§l✓ 已删除挑战塔: " + removed.getName());
        return true;
    }
    
    /**
     * 删除挑战塔（无玩家参数版本）
     */
    public boolean deleteTower(String towerId) {
        if (!towers.containsKey(towerId)) {
            return false;
        }
        
        // 检查是否有活动实例
        if (activeInstances.containsKey(towerId)) {
            return false;
        }
        
        // 从队列中移除
        queue.cleanupEmptyQueues();
        
        // 删除挑战塔
        towers.remove(towerId);
        
        // 保存到文件
        saveTowers();
        
        return true;
    }
    
    /**
     * 检查挑战塔是否存在
     */
    public boolean towerExists(String towerId) {
        return towers.containsKey(towerId);
    }
    
    /**
     * 检查挑战塔是否正在进行中
     */
    public boolean isTowerActive(String towerId) {
        return activeInstances.containsKey(towerId);
    }
    
    /**
     * 获取挑战塔
     */
    public ChallengeTower getTower(String towerId) {
        return towers.get(towerId);
    }
    
    /**
     * 获取所有挑战塔
     */
    public Collection<ChallengeTower> getAllTowers() {
        return towers.values();
    }
    
    /**
     * 获取活动实例
     */
    public ChallengeTowerInstance getActiveInstance(String towerId) {
        return activeInstances.get(towerId);
    }
    
    /**
     * 开始挑战塔实例
     */
    public boolean startTowerInstance(String towerId, List<Player> players) {
        ChallengeTower tower = getTower(towerId);
        if (tower == null) {
            return false;
        }
        
        // 检查是否已有活动实例
        if (activeInstances.containsKey(towerId)) {
            return false;
        }
        
        // 创建新实例
        ChallengeTowerInstance instance = new ChallengeTowerInstance(plugin, tower);
        instance.setChallengeTowerManager(this);
        
        // 添加到活动实例
        activeInstances.put(towerId, instance);
        
        // 开始挑战
        return instance.startChallenge(players);
    }
    
    /**
     * 结束挑战塔实例
     */
    public void endTowerInstance(String towerId, boolean success) {
        ChallengeTowerInstance instance = activeInstances.remove(towerId);
        if (instance != null) {
            instance.endChallenge(success);
        }
    }
    
    /**
     * 获取玩家进度
     */
    public ChallengeTowerPlayerProgress getPlayerProgress(UUID playerId) {
        return playerProgress.computeIfAbsent(playerId, id -> {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                return new ChallengeTowerPlayerProgress(player);
            } else {
                return new ChallengeTowerPlayerProgress(id, "未知玩家");
            }
        });
    }
    
    /**
     * 重置玩家进度
     */
    public boolean resetPlayerProgress(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!playerProgress.containsKey(playerId)) {
            player.sendMessage(ChatColor.YELLOW + "该玩家没有挑战塔进度记录");
            return false;
        }
        
        ChallengeTowerPlayerProgress progress = playerProgress.get(playerId);
        progress.resetProgress();
        
        player.sendMessage(ChatColor.GREEN + "§l✓ 已重置挑战塔进度");
        return true;
    }
    
    /**
     * 给予区域选择工具
     */
    public void giveSelectionTool(Player player) {
        selectionTool.giveSelectionTool(player);
    }
    
    /**
     * 获取选择的区域
     */
    public ChallengeTowerArea getSelectedArea(Player player) {
        return selectionTool.getSelectedArea(player);
    }
    
    /**
     * 可视化显示区域
     */
    public void visualizeArea(Player player, ChallengeTowerArea area) {
        selectionTool.visualizeArea(player, area);
    }
    
    /**
     * 事件处理：怪物死亡
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        org.bukkit.entity.Entity entity = event.getEntity();
        
        // 检查是否是挑战塔怪物
        if (entity.getScoreboardTags().contains("challenge_tower_monster")) {
            // 找到对应的实例
            for (ChallengeTowerInstance instance : activeInstances.values()) {
                if (instance.getSpawnedMonsters().contains(entity.getUniqueId())) {
                    instance.onMonsterDeath((LivingEntity) entity);
                    break;
                }
            }
        }
    }
    
    /**
     * 事件处理：玩家死亡
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();
        
        // 处理通天层玩家死亡
        onPlayerDeath(playerId);
    }
    
    /**
     * 事件处理：玩家移动（区域保护）
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null || from.getBlock().equals(to.getBlock())) {
            return;
        }
        
        // 检查玩家是否在挑战塔区域中
        for (ChallengeTower tower : towers.values()) {
            if (tower.getArea().contains(to)) {
                // 玩家进入了挑战塔区域
                
                // 检查玩家是否在队列中或活动中
                boolean isAllowed = false;
                
                // 检查队列
                String queueTowerId = queue.getPlayerQueue(player.getUniqueId());
                if (tower.getId().equals(queueTowerId)) {
                    isAllowed = true;
                }
                
                // 检查活动实例
                ChallengeTowerInstance instance = activeInstances.get(tower.getId());
                if (instance != null && instance.getPlayerIds().contains(player.getUniqueId())) {
                    isAllowed = true;
                }
                
                // 检查是否是管理员
                if (player.hasPermission("upmobs.admin")) {
                    isAllowed = true;
                }
                
                if (!isAllowed) {
                    // 玩家不允许进入，准备传送回去
                    handleUnauthorizedEntry(player, from, tower);
                    return;
                }
            }
        }
    }
    
    /**
     * 处理未授权进入
     */
    private void handleUnauthorizedEntry(Player player, Location entryLocation, ChallengeTower tower) {
        // 发送警告
        if (defaultConfig.isEnableWarning()) {
            player.sendMessage(ChatColor.RED + "警告: 你未在挑战塔队列中，5秒后将被传送出去");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        }
        
        // 延迟传送
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && tower.getArea().contains(player.getLocation())) {
                player.teleport(entryLocation);
                player.sendMessage(ChatColor.RED + "你未在挑战塔队列中，已传送回入口");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        }, defaultConfig.getTeleportDelayTicks());
    }
    
    /**
     * 事件处理：玩家退出
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // 从队列中移除
        queue.forceRemovePlayer(playerId);
        
        // 检查玩家是否在挑战塔活动中
        for (ChallengeTowerInstance instance : activeInstances.values()) {
            if (instance.getPlayerIds().contains(playerId)) {
                // 玩家在挑战塔活动中退出，可以记录或处理
                // 这里简单地从实例中移除玩家
                // 实际可能需要更复杂的处理
            }
        }
        
        // 检查玩家是否在通天层中
        onPlayerQuit(playerId);
        
        // 保存玩家进度
        if (playerProgress.containsKey(playerId)) {
            playerProgress.get(playerId).saveProgress();
        }
    }
    
    /**
     * 停止管理器
     */
    public void stop() {
        // 停止队列系统
        if (queue != null) {
            queue.stop();
        }
        
        // 结束所有挑战塔活动实例
        for (String towerId : new ArrayList<>(activeInstances.keySet())) {
            endTowerInstance(towerId, false);
        }
        
        // 结束所有通天层挑战
        forceEndAllSkyTowerChallenges();
        
        // 保存所有数据
        saveAll();
        
        plugin.getLogger().info("挑战塔管理器已停止");
    }
    
    // ========== Getter方法 ==========
    
    public Upmobs getPlugin() {
        return plugin;
    }
    
    public ChallengeTowerQueue getQueue() {
        return queue;
    }
    
    public AreaSelectionTool getSelectionTool() {
        return selectionTool;
    }
    
    public ChallengeTowerConfig getDefaultConfig() {
        return defaultConfig;
    }
    
    public void setDefaultConfig(ChallengeTowerConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }
    
    public Map<String, ChallengeTower> getTowers() {
        return new HashMap<>(towers);
    }
    
    public Map<String, ChallengeTowerInstance> getActiveInstances() {
        return new HashMap<>(activeInstances);
    }
    
    public Map<UUID, ChallengeTowerPlayerProgress> getPlayerProgressMap() {
        return new HashMap<>(playerProgress);
    }
    
    /**
     * 获取管理器状态信息
     */
    public String getStatusInfo() {
        return String.format("挑战塔: %d个, 活动实例: %d个, 通天层实例: %d个, 玩家进度: %d个, 队列: %d个",
            towers.size(), activeInstances.size(), activeSkyTowerInstances.size(),
            playerProgress.size(), queue.getAllQueueInfo().size());
    }
    
    // ========== 通天层相关方法 ==========
    
    /**
     * 开始通天层挑战
     */
    public boolean startSkyTowerChallenge(Player player, String towerId) {
        ChallengeTower tower = towers.get(towerId);
        if (tower == null) {
            player.sendMessage(ChatColor.RED + "§c挑战塔不存在！");
            return false;
        }
        
        // 检查玩家是否已经在通天层中
        if (activeSkyTowerInstances.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "§c你已经在通天层挑战中！");
            return false;
        }
        
        // 检查通天层是否启用
        SkyTowerConfig skyTowerConfig = tower.getSkyTowerConfig();
        if (!skyTowerConfig.isEnabled()) {
            player.sendMessage(ChatColor.RED + "§c该挑战塔的通天层未启用！");
            return false;
        }
        
        // 创建通天层实例
        SkyTowerInstance skyTowerInstance = new SkyTowerInstance(plugin, tower, player);
        
        // 开始挑战
        boolean started = skyTowerInstance.startChallenge();
        if (started) {
            activeSkyTowerInstances.put(player.getUniqueId(), skyTowerInstance);
            player.sendMessage(ChatColor.GREEN + "§a通天层挑战已开始！祝你好运！");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "§c无法开始通天层挑战！");
            return false;
        }
    }
    
    /**
     * 结束通天层挑战
     */
    public void endSkyTowerChallenge(UUID playerId, boolean success) {
        SkyTowerInstance instance = activeSkyTowerInstances.remove(playerId);
        if (instance != null) {
            instance.completeChallenge(success);
        }
    }
    
    /**
     * 强制结束所有通天层挑战
     */
    public void forceEndAllSkyTowerChallenges() {
        for (SkyTowerInstance instance : activeSkyTowerInstances.values()) {
            instance.forceEnd();
        }
        activeSkyTowerInstances.clear();
    }
    
    /**
     * 处理玩家死亡事件（通天层）
     */
    public void onPlayerDeath(UUID playerId) {
        SkyTowerInstance instance = activeSkyTowerInstances.get(playerId);
        if (instance != null) {
            instance.onPlayerDeath();
            activeSkyTowerInstances.remove(playerId);
        }
    }
    
    /**
     * 处理玩家退出事件（通天层）
     */
    public void onPlayerQuit(UUID playerId) {
        SkyTowerInstance instance = activeSkyTowerInstances.get(playerId);
        if (instance != null) {
            instance.forceEnd();
            activeSkyTowerInstances.remove(playerId);
        }
    }
    
    /**
     * 通天层完成回调
     */
    public void onSkyTowerComplete(SkyTowerInstance instance) {
        activeSkyTowerInstances.remove(instance.getPlayerId());
        
        // 这里可以保存排行榜数据等
        plugin.getLogger().info(String.format(
            "通天层完成: 玩家=%s, 时间=%.1f分钟, 击杀=%d, 奖励=%.1f",
            instance.getPlayerName(), instance.getElapsedTime() / 60000.0,
            instance.getTotalMonstersKilled(), instance.getTotalRewards()
        ));
    }
    
    /**
     * 获取玩家的通天层实例
     */
    public SkyTowerInstance getPlayerSkyTowerInstance(UUID playerId) {
        return activeSkyTowerInstances.get(playerId);
    }
    
    /**
     * 检查玩家是否在通天层中
     */
    public boolean isPlayerInSkyTower(UUID playerId) {
        return activeSkyTowerInstances.containsKey(playerId);
    }
    
    /**
     * 获取通天层状态信息
     */
    public String getSkyTowerStatusInfo() {
        if (activeSkyTowerInstances.isEmpty()) {
            return "§7当前没有进行中的通天层挑战";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("§6=== 通天层挑战状态 ===\n");
        
        for (SkyTowerInstance instance : activeSkyTowerInstances.values()) {
            info.append(String.format("§e%s: §f%.1f分钟, §f%d击杀, §f%.1f奖励\n",
                instance.getPlayerName(),
                instance.getElapsedTime() / 60000.0,
                instance.getTotalMonstersKilled(),
                instance.getTotalRewards()
            ));
        }
        
        return info.toString();
    }
}